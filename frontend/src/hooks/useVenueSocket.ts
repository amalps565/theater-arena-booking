import { Client } from '@stomp/stompjs'
import { useEffect } from 'react'
import { api } from '../api/client'
import type { VenueMessage } from '../api/types'
import { useSeatStore } from '../store/seatStore'
import { useToastStore } from '../store/toastStore'

const VENUE_TOPIC = '/topic/venue'
const RECONNECT_MS = 2000
const HEARTBEAT_MS = 10000

function brokerUrl(): string {
  const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${scheme}://${window.location.host}/ws`
}

export function useVenueSocket(): void {
  useEffect(() => {
    let buffering = true
    let buffered: VenueMessage[] = []
    let pending: VenueMessage[] = []
    let frame = 0
    let disposed = false

    const flush = () => {
      frame = 0
      const batch = pending
      pending = []
      useSeatStore.getState().applyMessages(batch)
    }

    const receive = (message: VenueMessage) => {
      if (buffering) {
        buffered.push(message)
        return
      }
      pending.push(message)
      if (!frame) {
        frame = requestAnimationFrame(flush)
      }
    }

    const loadSnapshot = async () => {
      if (disposed) {
        return
      }
      buffering = true
      buffered = []
      try {
        const [venue, mine] = await Promise.all([api.venue(), api.myHolds()])
        if (disposed) {
          return
        }
        const store = useSeatStore.getState()
        store.loadSnapshot(venue, mine.holds)
        store.applyMessages(buffered)
        buffered = []
        buffering = false
        store.setConnection('live')
      } catch {
        if (!disposed) {
          useToastStore.getState().show('error', 'Could not load the arena. Retrying…')
          setTimeout(() => void loadSnapshot(), RECONNECT_MS)
        }
      }
    }

    const client = new Client({
      brokerURL: brokerUrl(),
      reconnectDelay: RECONNECT_MS,
      heartbeatIncoming: HEARTBEAT_MS,
      heartbeatOutgoing: HEARTBEAT_MS,
      onConnect: () => {
        client.subscribe(VENUE_TOPIC, (frameMessage) => {
          receive(JSON.parse(frameMessage.body) as VenueMessage)
        })
        void loadSnapshot()
      },
      onWebSocketClose: () => {
        if (!disposed) {
          buffering = true
          useSeatStore.getState().setConnection('reconnecting')
        }
      },
    })
    client.activate()

    return () => {
      disposed = true
      if (frame) {
        cancelAnimationFrame(frame)
      }
      void client.deactivate()
    }
  }, [])
}
