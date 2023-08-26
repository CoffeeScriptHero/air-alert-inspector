package com.kozarenko.bot.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.kozarenko.bot.util.Constants.UKRAINE_ZONE_ID;

public class State {

  private static final String ALERT_TRUE = "\uD83D\uDD34 Повітряна тривога в ";
  private static final String ALERT_FALSE = "\uD83D\uDFE2 Відбій тривоги в ";
  private static final String KYIV = "м. Київ";

  private String name;
  private int id;
  private boolean alert;
  private OffsetDateTime changed;

  public State() {
  }

  public State(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public State(String name, int id, OffsetDateTime changed, boolean alert) {
    this.name = name;
    this.id = id;
    this.changed = changed;
    this.alert = alert;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public OffsetDateTime getChanged() {
    return changed;
  }

  public void setChanged(OffsetDateTime changed) {
    this.changed = changed;
  }

  public boolean isAlert() {
    return alert;
  }

  public void setAlert(boolean alert) {
    this.alert = alert;
  }

  public String getShortName() {
    String space = " ";
    if (!name.equals(KYIV) && name.contains(space)) {
      return name.substring(0, name.indexOf(space));
    }
    return name;
  }

  public String getAlertMessage() {
    return (alert ? ALERT_TRUE : ALERT_FALSE)
            + name
            + "\n"
            + getAlertDateTimeMessage()
            + "\n"
            + getStateHashtag();
  }

  private String getAlertDateTimeMessage() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss, dd-MM-yyyy").withZone(UKRAINE_ZONE_ID);
    return "Час: " + changed.format(dtf);
  }

  private String getStateHashtag() {
    return getHashtagName() + " " + getHashtagDate();
  }

  private String getHashtagName() {
    String cityPrefix = "м. ";
    String cityDelimiter = " ";
    String stateDelimiter = " ";

    if (name.startsWith(cityPrefix)) {
      return "#м_" + name.substring(name.indexOf(cityDelimiter) + 1);
    }

    int delimiterIndex = name.indexOf(stateDelimiter);

    return "#" + name.substring(0, delimiterIndex) + "_" + name.substring(delimiterIndex + 1);
  }

  private String getHashtagDate() {
    int day = changed.getDayOfMonth();
    String month = changed.getMonth().getDisplayName(TextStyle.FULL, new Locale("uk"));
    int year = changed.getYear();

    return String.format("#%d_%s_%d", day, month, year);
  }

  @Override
  public String toString() {
    return String.format("State{name='%s', id=%d, changed=%s, alert=%b}", name, id, changed, alert);
  }
}
