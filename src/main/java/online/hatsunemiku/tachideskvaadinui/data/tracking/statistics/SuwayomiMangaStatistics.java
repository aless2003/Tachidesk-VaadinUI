/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tracking.statistics;

import java.time.ZoneId;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.TrackRecord;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;

public class SuwayomiMangaStatistics implements MangaStatistics {
  private final int status;
  private final float progress;
  private final float score;
  private final MediaDate startedAt;
  private final MediaDate completedAt;

  public SuwayomiMangaStatistics(TrackRecord record) {
    this.status = record.getStatus();
    this.progress = record.getLastChapterRead();
    this.score = record.getScore();

    if (record.getStartDate() == null) {
      this.startedAt = null;
    } else {
      var start = record.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate();
      this.startedAt = new MediaDate(start);
    }

    if (record.getFinishDate() == null) {
      this.completedAt = null;
    } else {
      var finish = record.getFinishDate().atZone(ZoneId.systemDefault()).toLocalDate();
      this.completedAt = new MediaDate(finish);
    }
  }

  @Override
  public double progress() {
    return progress;
  }

  @Override
  public double score() {
    return score;
  }

  @Override
  public MediaDate startedAt() {
    return startedAt;
  }

  @Override
  public MediaDate completedAt() {
    return completedAt;
  }

  public int status() {
    return status;
  }


}
