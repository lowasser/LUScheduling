package org.learningu.scheduling.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Shuffle {
  private Shuffle() {
  }

  public static <E> List<E> shuffleK(List<E> list, int k, Random rnd) {
    for (int i = 0; i < k; i++) {
      int j = rnd.nextInt(list.size() - k + 1) + k - 1;
      Collections.swap(list, i, j);
    }
    return list.subList(0, k);
  }
}
