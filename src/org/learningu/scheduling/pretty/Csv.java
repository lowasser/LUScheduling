package org.learningu.scheduling.pretty;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.escape.Escapers;

public final class Csv {
  private final ImmutableList<Row> contents;

  private Csv(ImmutableList<Row> contents) {
    this.contents = contents;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static RowBuilder newRowBuilder() {
    return new RowBuilder();
  }

  @Override
  public String toString() {
    return Joiner.on('\n').join(contents);
  }

  public static final class Builder {
    private final ImmutableList.Builder<Row> contents;

    private Builder() {
      this.contents = ImmutableList.builder();
    }

    public Builder add(Row row) {
      contents.add(row);
      return this;
    }

    public Csv build() {
      return new Csv(contents.build());
    }
  }

  public static final class Row {
    private final ImmutableList<String> contents;

    private Row(ImmutableList<String> contents) {
      this.contents = contents;
    }

    @Override
    public String toString() {
      return Joiner.on(',').join(contents);
    }
  }

  public static final class RowBuilder {
    private final ImmutableList.Builder<String> contents;

    private RowBuilder() {
      this.contents = ImmutableList.builder();
    }

    public RowBuilder add(String message, Object... args) {
      contents.add("\""
          + Escapers
              .builder()
              .addEscape('\"', "\"\"")
              .build()
              .escape(args.length == 0 ? message : String.format(message, args)) + "\"");
      return this;
    }

    public RowBuilder addBlank() {
      contents.add("");
      return this;
    }

    public Row build() {
      return new Row(contents.build());
    }
  }
}
