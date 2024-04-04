/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.katsute.mal4j.AccessToken;
import dev.katsute.mal4j.Authorization;
import dev.katsute.mal4j.MyAnimeList;
import dev.katsute.mal4j.MyAnimeListAuthenticator;
import dev.katsute.mal4j.PaginatedIterator;
import dev.katsute.mal4j.manga.Manga;
import dev.katsute.mal4j.manga.MangaListStatus;
import dev.katsute.mal4j.manga.property.MangaSort;
import dev.katsute.mal4j.manga.property.MangaStatus;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthData;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MyAnimeListAPIService {

  private static final Logger log = LoggerFactory.getLogger(MyAnimeListAPIService.class);
  private final String CLIENT_ID = "a039c56fb609cd33ebd59381a6e9b460";
  private final TrackingDataService tds;
  private final WebClient webClient;
  private final Cache<UUID, String> pkceCache;
  @Nullable private MyAnimeList mal;

  /**
   * Initializes an instance of the MyAnimeListAPIService class.
   *
   * @param tds        The {@link TrackingDataService} used for storing tokens.
   * @param webClient  The {@link WebClient} used for making requests to the MAL API.
   */
  public MyAnimeListAPIService(TrackingDataService tds, WebClient webClient) {
    this.tds = tds;
    this.webClient = webClient;
    this.pkceCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    if (!tds.getTokens().hasMalToken()) {
      return;
    }

    OAuthData data = tds.getTokens().getMalToken();

    if (data.getExpiresAsInstant().isBefore(Instant.now())) {
      var newData = refreshToken(data.getRefreshToken());
      tds.getTokens().setMalToken(newData);

      data = newData;
    }

    authenticateMALWithToken(data);
  }

  private void authenticateMALWithToken(OAuthData data) {
    this.mal = MyAnimeList.withToken(data.getAccessToken());
  }

  @NotNull
  public String getAuthUrl() {
    String baseUrl = "https://myanimelist.net/v1/oauth2/authorize";
    String responseType = "code";
    String codeChallenge = MyAnimeListAuthenticator.generatePKCE(128);

    UUID pkceId = UUID.randomUUID();

    pkceCache.put(pkceId, codeChallenge);

    String stateParam = "{\"pkceId\"=\"%s\"}";
    stateParam = URLEncoder.encode(stateParam.formatted(pkceId), StandardCharsets.UTF_8);

    String params = "response_type=%s&client_id=%s&code_challenge=%s&state=%s";
    params =
        params.formatted(responseType, CLIENT_ID, codeChallenge, pkceId.toString(), stateParam);

    return "%s?%s".formatted(baseUrl, params);
  }

  public boolean hasMalToken() {
    return tds.getTokens().hasMalToken();
  }

  public void exchangeCodeForTokens(String code, String pkceId) {

    String pkce = pkceCache.getIfPresent(UUID.fromString(pkceId));

    if (pkce == null) {
      throw new IllegalArgumentException("Invalid PKCE ID");
    }

    Authorization auth = new Authorization(CLIENT_ID, null, code, pkce);

    MyAnimeListAuthenticator oauth = new MyAnimeListAuthenticator(auth);
    AccessToken token = oauth.getAccessToken();

    OAuthData data = new OAuthData(token);

    tds.getTokens().setMalToken(data);

    authenticateMALWithToken(data);
  }

  private OAuthData refreshToken(String refreshToken) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);

    return webClient
        .post()
        .uri("https://myanimelist.net/v1/oauth2/token")
        .headers(headers -> headers.setBasicAuth(CLIENT_ID, ""))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(OAuthData.class)
        .block();
  }

  public List<Manga> getMangaWithStatus(MangaStatus status) {
    if (mal == null) {
      throw new IllegalStateException("Not authenticated with MAL");
    }

    PaginatedIterator<MangaListStatus> iter =
        mal.getUserMangaListing()
            .withStatus(status)
            .sortBy(MangaSort.Title)
            .includeNSFW()
            .searchAll();

    var list = new ArrayList<MangaListStatus>();

    while (iter.hasNext()) {
      list.add(iter.next());
    }

    log.debug("Got {} manga with status {}", list.size(), status.name());

    return list.stream().map(MangaListStatus::getManga).toList();
  }
}
