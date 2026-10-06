import { memo, useEffect, useRef, type PointerEvent as ReactPointerEvent } from 'react'
import { toggleSeat } from '../lib/seatActions'
import { useSeatStore } from '../store/seatStore'
import { SeatDot } from './SeatDot'
import { Tooltip, type TooltipHandle } from './Tooltip'

const MIN_ZOOM = 0.6
const MAX_ZOOM = 8
const WHEEL_ZOOM_STEP = 1.15
const BUTTON_ZOOM_STEP = 1.4
const DRAG_THRESHOLD_PX = 4
const STAGE_TOP = 20
const STAGE_HEIGHT = 60
const LABEL_OFFSET = 10

interface View {
  x: number
  y: number
  k: number
}

interface Drag {
  pointerId: number
  startX: number
  startY: number
  originX: number
  originY: number
  moved: boolean
}

function seatIdOf(target: EventTarget | null): number | null {
  if (target instanceof SVGElement && target.dataset.seatId) {
    return Number(target.dataset.seatId)
  }
  return null
}

function zoomView(current: View, factor: number, pointX: number, pointY: number): View {
  const k = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, current.k * factor))
  const ratio = k / current.k
  return { k, x: pointX - (pointX - current.x) * ratio, y: pointY - (pointY - current.y) * ratio }
}

function writeView(group: SVGGElement | null, { x, y, k }: View): void {
  group?.setAttribute('transform', `translate(${x} ${y}) scale(${k})`)
}

const SeatLayer = memo(function SeatLayer() {
  const seatIds = useSeatStore((s) => s.seatIds)
  return (
    <g>
      {seatIds.map((id) => (
        <SeatDot key={id} id={id} />
      ))}
    </g>
  )
})

const SectionLabels = memo(function SectionLabels() {
  const labels = useSeatStore((s) => s.labels)
  const sections = useSeatStore((s) => s.sections)
  return (
    <g className="pointer-events-none fill-slate-500 text-[11px] font-semibold tracking-wide uppercase">
      {labels.map((label) => (
        <text key={label.id} x={label.x - 4} y={label.y - LABEL_OFFSET}>
          {sections[label.id]?.name}
        </text>
      ))}
    </g>
  )
})

export function SeatMap() {
  const bounds = useSeatStore((s) => s.bounds)
  const svgRef = useRef<SVGSVGElement>(null)
  const groupRef = useRef<SVGGElement>(null)
  const tooltipRef = useRef<TooltipHandle>(null)
  const view = useRef<View>({ x: 0, y: 0, k: 1 })
  const drag = useRef<Drag | null>(null)
  const suppressClick = useRef(false)

  const applyView = () => writeView(groupRef.current, view.current)

  const zoomAt = (factor: number, pointX: number, pointY: number) => {
    view.current = zoomView(view.current, factor, pointX, pointY)
    applyView()
  }

  useEffect(() => {
    const svg = svgRef.current
    if (!svg) {
      return
    }
    const onWheel = (event: WheelEvent) => {
      event.preventDefault()
      const ctm = svg.getScreenCTM()
      if (!ctm) {
        return
      }
      const point = new DOMPoint(event.clientX, event.clientY).matrixTransform(ctm.inverse())
      const factor = event.deltaY < 0 ? WHEEL_ZOOM_STEP : 1 / WHEEL_ZOOM_STEP
      view.current = zoomView(view.current, factor, point.x, point.y)
      writeView(groupRef.current, view.current)
      tooltipRef.current?.hide()
    }
    svg.addEventListener('wheel', onWheel, { passive: false })
    return () => svg.removeEventListener('wheel', onWheel)
  }, [])

  const onPointerDown = (event: ReactPointerEvent<SVGSVGElement>) => {
    drag.current = {
      pointerId: event.pointerId,
      startX: event.clientX,
      startY: event.clientY,
      originX: view.current.x,
      originY: view.current.y,
      moved: false,
    }
  }

  const onPointerMove = (event: ReactPointerEvent<SVGSVGElement>) => {
    const active = drag.current
    if (!active || active.pointerId !== event.pointerId) {
      return
    }
    const dx = event.clientX - active.startX
    const dy = event.clientY - active.startY
    if (!active.moved && Math.hypot(dx, dy) < DRAG_THRESHOLD_PX) {
      return
    }
    if (!active.moved) {
      active.moved = true
      event.currentTarget.setPointerCapture(event.pointerId)
      tooltipRef.current?.hide()
    }
    const unitsPerPixel = 1 / (event.currentTarget.getScreenCTM()?.a ?? 1)
    view.current = {
      ...view.current,
      x: active.originX + dx * unitsPerPixel,
      y: active.originY + dy * unitsPerPixel,
    }
    applyView()
  }

  const onPointerUp = (event: ReactPointerEvent<SVGSVGElement>) => {
    if (drag.current?.moved) {
      suppressClick.current = true
      event.currentTarget.releasePointerCapture(event.pointerId)
    }
    drag.current = null
  }

  const onPointerOver = (event: ReactPointerEvent<SVGSVGElement>) => {
    if (drag.current?.moved) {
      return
    }
    const seatId = seatIdOf(event.target)
    if (seatId === null) {
      tooltipRef.current?.hide()
    } else {
      tooltipRef.current?.show(seatId, event.clientX, event.clientY)
    }
  }

  const onClick = (event: React.MouseEvent<SVGSVGElement>) => {
    if (suppressClick.current) {
      suppressClick.current = false
      return
    }
    const seatId = seatIdOf(event.target)
    if (seatId !== null) {
      void toggleSeat(seatId)
    }
  }

  const zoomFromCenter = (factor: number) => zoomAt(factor, bounds.width / 2, bounds.height / 2)

  const resetView = () => {
    view.current = { x: 0, y: 0, k: 1 }
    applyView()
  }

  return (
    <div className="relative h-full w-full">
      <svg
        ref={svgRef}
        viewBox={`0 0 ${bounds.width} ${bounds.height}`}
        className="h-full w-full cursor-grab touch-none select-none active:cursor-grabbing"
        role="img"
        aria-label="Arena seating map"
        onPointerDown={onPointerDown}
        onPointerMove={onPointerMove}
        onPointerUp={onPointerUp}
        onPointerCancel={onPointerUp}
        onPointerOver={onPointerOver}
        onPointerLeave={() => tooltipRef.current?.hide()}
        onClick={onClick}
      >
        <g ref={groupRef}>
          <rect
            x={bounds.width * 0.25}
            y={STAGE_TOP}
            width={bounds.width * 0.5}
            height={STAGE_HEIGHT}
            rx={12}
            className="fill-slate-800"
          />
          <text
            x={bounds.width / 2}
            y={STAGE_TOP + STAGE_HEIGHT / 2 + 6}
            textAnchor="middle"
            className="pointer-events-none fill-white text-lg font-bold tracking-[0.4em]"
          >
            STAGE
          </text>
          <SectionLabels />
          <SeatLayer />
        </g>
      </svg>
      <div className="absolute top-3 right-3 flex flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow">
        <button
          type="button"
          className="px-3 py-1.5 text-lg hover:bg-slate-100"
          aria-label="Zoom in"
          onClick={() => zoomFromCenter(BUTTON_ZOOM_STEP)}
        >
          +
        </button>
        <button
          type="button"
          className="border-t border-slate-200 px-3 py-1.5 text-lg hover:bg-slate-100"
          aria-label="Zoom out"
          onClick={() => zoomFromCenter(1 / BUTTON_ZOOM_STEP)}
        >
          −
        </button>
        <button
          type="button"
          className="border-t border-slate-200 px-3 py-1.5 text-xs hover:bg-slate-100"
          onClick={resetView}
        >
          Reset
        </button>
      </div>
      <Tooltip ref={tooltipRef} />
    </div>
  )
}
