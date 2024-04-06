/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiTrackingClient;
import org.springframework.stereotype.Service;

/**
 * Represents a Service for handling tracking requests to the Suwayomi Server. This service is a
 * wrapper around the {@link SuwayomiTrackingClient} class and provides methods to abstract the
 * inner workings of the client. Also provides convenience methods for handling tracking requests.
 */
@Service
public class SuwayomiTrackingService {

  private final SuwayomiTrackingClient client;

  /**
   * Represents a Suwayomi Tracking Service.
   *
   * @param client the {@link SuwayomiTrackingClient} used for handling tracking requests to the
   *               Suwayomi Server.
   */
  public SuwayomiTrackingService(SuwayomiTrackingClient client) {
    this.client = client;
  }

  /**
   * Checks if the Suwayomi tracking service is authenticated with AniList.
   *
   * @return true if the Suwayomi tracking service is authenticated with AniList, false otherwise
   */
  public boolean isAniListAuthenticated() {
    int id = TrackerType.ANILIST.id;
    return client.isTrackerLoggedIn(id);
  }

  /**
   * Checks if the Suwayomi tracking service is authenticated with MyAnimeList (MAL).
   *
   * @return true if the Suwayomi tracking service is authenticated with MAL, false otherwise
   */
  public boolean isMALAuthenticated() {
    int id = TrackerType.MAL.id;
    return client.isTrackerLoggedIn(id);
  }

  /**
   * Constructs the AniList authentication URL. This URL is used to authenticate the User with the
   * AniList API.
   *
   * @return the AniList authentication URL as a string
   */
  public String getAniListAuthUrl() {
    int id = TrackerType.ANILIST.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  /**
   * Constructs the MyAnimeList (MAL) authentication URL. This URL is used to authenticate the User
   * with the MAL API.
   *
   * @return the MAL authentication URL as a string
   */
  public String getMALAuthUrl() {
    int id = TrackerType.MAL.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  /**
   * Searches for manga on AniList using the provided query.
   *
   * @param query the search query for manga
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   */
  public List<TrackerSearchResult> searchAniList(String query) {
    int id = TrackerType.ANILIST.id;
    return client.searchTracker(query, id);
  }

  /**
   * Searches for manga on MyAnimeList (MAL) using the provided query.
   *
   * @param query the search query for manga
   * @return a list of {@link TrackerSearchResult} objects representing the search results
   */
  public List<TrackerSearchResult> searchMAL(String query) {
    int id = TrackerType.MAL.id;
    return client.searchTracker(query, id);
  }

  /**
   * Tracks a manga on AniList using the provided manga ID and external ID.
   *
   * @param mangaId    the ID of the manga to be tracked
   * @param externalId the external ID of the manga on AniList
   */
  public void trackOnAniList(int mangaId, int externalId) {
    int id = TrackerType.ANILIST.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  /**
   * Tracks a manga on MyAnimeList (MAL) using the provided manga ID and external ID.
   *
   * @param mangaId    the ID of the manga to be tracked
   * @param externalId the external ID of the manga on MAL
   */
  public void trackOnMAL(int mangaId, int externalId) {
    int id = TrackerType.MAL.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  /**
   * Logs in to the Suwayomi tracker with the specified URL and tracker ID.
   *
   * @param url       the URL used for the login callback
   * @param trackerId the ID of the tracker to log in to
   */
  public void loginSuwayomi(String url, int trackerId) {
    client.loginTracker(url, trackerId);
  }

  /**
   * Retrieves the state authentication parameter for the specified tracker.
   *
   * @param id the ID of the tracker
   * @return the state authentication parameter as a formatted string
   */
  private String getStateAuthParam(int id) {

    String jsonTemplate =
        """
            {\
            "redirectUrl":"http://localhost:8080/validate/suwayomi",\
            "trackerId":%d,\
            "anyOtherInfo":"%s"\
            }\
            """
            .strip();

    String template = "&state=%s";

    TrackerType trackerType = TrackerType.fromId(id);

    if (trackerType == null) {
      throw new IllegalArgumentException("Invalid tracker ID");
    }

    String trackerName = trackerType.name();

    String json = jsonTemplate.formatted(id, trackerName);

    return template.formatted(json);
  }

  /**
   * An enumeration representing different types of trackers.
   */
  @Getter
  private enum TrackerType {
    MAL(1),
    ANILIST(2);

    private final int id;

    /**
     * Instantiates a TrackerType object with the specified id.
     *
     * @param id the id of the Tracker on the Suwayomi Server
     */
    TrackerType(int id) {
      this.id = id;
    }

    /**
     * Retrieves the {@link TrackerType} object based on the provided id.
     *
     * @param id the id of the TrackerType
     * @return the corresponding {@link TrackerType} object, or null if no match is found
     */
    @Nullable
    public static TrackerType fromId(int id) {
      var match =
          Arrays.stream(TrackerType.values())
              .filter(trackerType -> trackerType.id == id)
              .findFirst();

      return match.orElse(null);
    }
  }
}
