package com.arena.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arena.auth.service.DemoUserSeeder;
import com.arena.support.IntegrationTestSupport;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AuthFlowTest extends IntegrationTestSupport {

  private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
  private static final long SEAT_ID = 520L;

  @Autowired private MockMvc mvc;

  @Test
  void aDemoAccountSignsInAndReceivesAToken() throws Exception {
    mvc.perform(login("alice", DemoUserSeeder.DEMO_PASSWORD))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username").value("alice"))
        .andExpect(jsonPath("$.token").isNotEmpty());
  }

  @Test
  void aWrongPasswordIsRefused() throws Exception {
    mvc.perform(login("alice", "not-the-password"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
  }

  @Test
  void holdingWithoutSigningInIsRefused() throws Exception {
    mvc.perform(holdRequest(SEAT_ID))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

    assertThat(countSeatsWithStatus(SEAT_ID, "AVAILABLE")).isEqualTo(1);
  }

  @Test
  void aForgedTokenIsRefused() throws Exception {
    mvc.perform(holdRequest(SEAT_ID).header(HttpHeaders.AUTHORIZATION, "Bearer forged.token.value"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void aSignedInHoldBelongsToTheAccount() throws Exception {
    String token = tokenFor("alice");
    String aliceId =
        jdbcTemplate.queryForObject(
            "select cast(id as varchar) from app_user where username = 'alice'", String.class);

    mvc.perform(holdRequest(SEAT_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isCreated());

    String heldBy =
        jdbcTemplate.queryForObject(
            "select cast(held_by as varchar) from seat where id = ?", String.class, SEAT_ID);
    assertThat(heldBy).isEqualTo(aliceId);
    mvc.perform(get("/api/holds/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.holds[0].seatId").value(SEAT_ID));
  }

  @Test
  void anotherAccountCannotTakeAHeldSeat() throws Exception {
    mvc.perform(
            holdRequest(SEAT_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor("alice")))
        .andExpect(status().isCreated());

    mvc.perform(holdRequest(SEAT_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor("bob")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SEAT_TAKEN"));
  }

  @Test
  void aNewAccountCanRegisterAndThenSignIn() throws Exception {
    String username = "fan_" + UUID.randomUUID().toString().substring(0, 8);
    String body =
        "{\"username\":\"%s\",\"displayName\":\"New Fan\",\"password\":\"long-enough-pw\"}"
            .formatted(username);

    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.user.displayName").value("New Fan"));

    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("USERNAME_TAKEN"));
    mvc.perform(login(username, "long-enough-pw")).andExpect(status().isOk());
  }

  @Test
  void storedPasswordsAreHashed() {
    String hash =
        jdbcTemplate.queryForObject(
            "select password_hash from app_user where username = 'alice'", String.class);

    assertThat(hash).doesNotContain(DemoUserSeeder.DEMO_PASSWORD).startsWith("$2");
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(
      String username, String password) {
    return post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password));
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder holdRequest(
      long seatId) {
    return post("/api/holds")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"seatIds\":[%d]}".formatted(seatId));
  }

  private String tokenFor(String username) throws Exception {
    String body =
        mvc.perform(login(username, DemoUserSeeder.DEMO_PASSWORD))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    Matcher matcher = TOKEN.matcher(body);
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }
}
