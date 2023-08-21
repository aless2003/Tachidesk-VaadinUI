package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import lombok.Getter;

@Getter
public enum AniListScoreFormat {
  POINT_100(100, 1),
  POINT_10_DECIMAL(10, 1),
  POINT_10(10, 1),
  POINT_5(5, 1),
  POINT_3(3, 1);

  private final int maxScore;
  @Getter
  private final int minScore;

  AniListScoreFormat(int maxScore, int minScore) {
    this.maxScore = maxScore;
    this.minScore = minScore;
  }
}
