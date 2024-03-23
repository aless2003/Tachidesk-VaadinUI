/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.tracker;

import javax.sound.midi.Track;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.search.TrackerSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.client.suwayomi.SuwayomiTrackingClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SuwayomiTrackingService {
  private final SuwayomiTrackingClient client;
  private final SettingsService settingsService;

  public SuwayomiTrackingService(SuwayomiTrackingClient client, SettingsService settingsService) {
    this.client = client;
    this.settingsService = settingsService;
  }

  public boolean isAniListAuthenticated() {
    int id = TrackerType.ANILIST.id;
    return client.isTrackerLoggedIn(id);
  }

  public boolean isMALAuthenticated() {
    int id = TrackerType.MAL.id;
    return client.isTrackerLoggedIn(id);
  }

  public String getAniListAuthUrl() {
    int id = TrackerType.ANILIST.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  public String getMALAuthUrl() {
    int id = TrackerType.MAL.id;
    return client.getTrackerAuthUrl(id) + getStateAuthParam(id);
  }

  public List<TrackerSearchResult> searchAniList(String query) {
    int id = TrackerType.ANILIST.id;
    return client.searchTracker(query, id);
  }

    public List<TrackerSearchResult> searchMAL(String query) {
        int id = TrackerType.MAL.id;
        return client.searchTracker(query, id);
    }

  public void trackOnAniList(int mangaId, int externalId) {
    int id = TrackerType.ANILIST.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }

  public void trackOnMAL(int mangaId, int externalId) {
    int id = TrackerType.MAL.id;
    client.trackMangaOnTracker(mangaId, externalId, id);
  }


  public void loginSuwayomi(String url, int trackerId) {
    client.loginTracker(url, trackerId);
  }

  private String getStateAuthParam(int id) {

      String jsonTemplate =
        """
    {\
    "redirectUrl":"http://localhost:8080/validate/suwayomi",\
    "trackerId":%d,\
    "anyOtherInfo":"%s"\
    }\
    """.strip();

    String template = "&state=%s";

    String trackerName = TrackerType.fromId(id).name();

    String json = jsonTemplate.formatted(id, trackerName);

    return template.formatted(json);
  }

  @Getter
  private enum TrackerType {
    MAL(1),
    ANILIST(2);

    private final int id;

    TrackerType(int id) {
      this.id = id;
    }

    public static TrackerType fromId(int id) {
      var match = Arrays.stream(TrackerType.values())
              .filter(trackerType -> trackerType.id == id)
              .findFirst();

        return match.orElse(null);

    }
  }
}
