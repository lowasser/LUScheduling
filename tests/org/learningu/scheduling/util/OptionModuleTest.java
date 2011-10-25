package org.learningu.scheduling.util;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import junit.framework.TestCase;

import org.apache.commons.cli.ParseException;
import org.joda.time.Period;
import org.learningu.scheduling.OptionsModule;
import org.learningu.scheduling.Flag;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class OptionModuleTest extends TestCase {
  private static final class SampleFlags {
    @Flag("someInteger")
    private int someInteger;

    @Flag("someDouble")
    private double someDouble;

    @Flag("someBigInteger")
    private BigInteger someBigInteger;

    @Flag("somePeriod")
    private Period somePeriod;

    @Flag("someString")
    private String someString;

    @Flag("boolFlag")
    private boolean boolFlag;

    @Flag(value = "modes", multiple = true)
    private List<RoundingMode> modes;

    @Inject
    SampleFlags(@Named("someInteger") int someInteger, @Named("someDouble") double someDouble,
        @Named("someBigInteger") BigInteger someBigInteger,
        @Named("somePeriod") Period somePeriod, @Named("someString") String someString,
        @Named("boolFlag") boolean boolFlag, @Named("modes") List<RoundingMode> modes) {
      this.someInteger = someInteger;
      this.someDouble = someDouble;
      this.someBigInteger = someBigInteger;
      this.somePeriod = somePeriod;
      this.someString = someString;
      this.boolFlag = boolFlag;
      this.modes = modes;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (o instanceof SampleFlags) {
        SampleFlags other = (SampleFlags) o;
        return someInteger == other.someInteger && someDouble == other.someDouble
            && someBigInteger.equals(other.someBigInteger) && somePeriod.equals(other.somePeriod)
            && someString.equals(other.someString) && boolFlag == other.boolFlag
            && modes.equals(other.modes);
      }
      return false;
    }
  }

  public void testBasicParsing() throws ParseException {
    String[] args = { "-someInteger", "25", "--somePeriod", "1h1s", "--boolFlag", "--someString",
        "abcdefg", "--someDouble", "-5.0", "--someBigInteger", "0", "--modes=DOWN,UP" };
    Injector injector = Guice.createInjector(OptionsModule.create(args, SampleFlags.class));

    SampleFlags instance = injector.getInstance(SampleFlags.class);
    assertEquals(new SampleFlags(25, -5.0, BigInteger.ZERO, Period.hours(1).plusSeconds(1),
        "abcdefg", true, Arrays.asList(RoundingMode.DOWN, RoundingMode.UP)), instance);
  }
}
