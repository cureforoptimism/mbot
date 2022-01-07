package com.cureforoptimism.mbot.service;

import com.cureforoptimism.mbot.application.DiscordBot;
import com.cureforoptimism.mbot.domain.Floor;
import com.cureforoptimism.mbot.repository.FloorRepository;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FloorService {
  private final FloorRepository floorRepository;
  private final DiscordBot discordBot;
  static final Color bgColor = new Color(11, 10, 36);
  private BufferedImage imageSmol = null;
  @Getter private byte[] currentFloorImageBytes = null;
  @Getter private byte[] currentFloorUsdImageBytes = null;

  public FloorService(FloorRepository floorRepository, DiscordBot discordBot) {
    this.floorRepository = floorRepository;
    this.discordBot = discordBot;

    var quanticoFile = new ClassPathResource("Quantico-Regular.ttf");

    try {
      imageSmol = ImageIO.read(new ClassPathResource("smol.png").getInputStream());

      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      var chartFont = Font.createFont(Font.TRUETYPE_FONT, quanticoFile.getInputStream());
      ge.registerFont(chartFont);
    } catch (IOException | FontFormatException e) {
      // Handle exception
      log.error("unable to initialize font", e);
    }

    regenerateChart();
  }

  public void addFloorPrice(
      BigDecimal cheapestMale,
      BigDecimal cheapestFemale,
      BigDecimal landFloor,
      BigDecimal cheapestVroom,
      BigDecimal cheapestSwol) {
    Double magicPrice = discordBot.getCurrentPrice();

    if (magicPrice == null) {
      // This can happen on startup. Just persist on next update.
      return;
    }

    floorRepository.save(
        Floor.builder()
            .magicPrice(new BigDecimal(magicPrice))
            .maleFloor(cheapestMale)
            .femaleFloor(cheapestFemale)
            .landFloor(landFloor)
            .vroomFloor(cheapestVroom)
            .bodyFloor(cheapestSwol)
            .build());

    regenerateChart();
  }

  private synchronized void regenerateChart() {
    final var floorValuesLastDay =
        floorRepository.findByCreatedIsAfter(
            new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()));

    // Group by hour; take the average of all floor entries
    Map<Date, BigDecimal> maleFloors = new HashMap<>();
    Map<Date, BigDecimal> femaleFloors = new HashMap<>();
    Map<Date, BigDecimal> vroomFloors = new HashMap<>();
    Map<Date, BigDecimal> bodyFloors = new HashMap<>();
    Map<Date, BigDecimal> landFloors = new HashMap<>();

    Map<Date, BigDecimal> maleFloorsUsd = new HashMap<>();
    Map<Date, BigDecimal> femaleFloorsUsd = new HashMap<>();
    Map<Date, BigDecimal> vroomFloorsUsd = new HashMap<>();
    Map<Date, BigDecimal> bodyFloorsUsd = new HashMap<>();
    Map<Date, BigDecimal> landFloorsUsd = new HashMap<>();

    int hourlyFloors = 0;
    int lastHour = -1;
    BigDecimal avgMaleFloor = BigDecimal.ZERO;
    BigDecimal avgFemaleFloor = BigDecimal.ZERO;
    BigDecimal avgVroomFloor = BigDecimal.ZERO;
    BigDecimal avgBodyFloor = BigDecimal.ZERO;
    BigDecimal avgLandFloor = BigDecimal.ZERO;

    BigDecimal avgMaleFloorUsd = BigDecimal.ZERO;
    BigDecimal avgFemaleFloorUsd = BigDecimal.ZERO;
    BigDecimal avgVroomFloorUsd = BigDecimal.ZERO;
    BigDecimal avgBodyFloorUsd = BigDecimal.ZERO;
    BigDecimal avgLandFloorUsd = BigDecimal.ZERO;

    for (Floor floor : floorValuesLastDay) {
      final var localDateTime =
          Instant.ofEpochMilli(floor.getCreated().getTime())
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime();
      int hour = localDateTime.getHour();

      // Set to first hour in result set on first iteration
      if (lastHour == -1) {
        lastHour = hour;
      }

      if (hour == lastHour) {
        hourlyFloors++;
        avgMaleFloor = avgMaleFloor.add(floor.getMaleFloor());
        avgFemaleFloor = avgFemaleFloor.add(floor.getFemaleFloor());
        avgVroomFloor = avgVroomFloor.add(floor.getVroomFloor());
        avgBodyFloor = avgBodyFloor.add(floor.getBodyFloor());
        avgLandFloor = avgLandFloor.add(floor.getLandFloor());

        avgMaleFloorUsd = avgMaleFloorUsd.add(floor.getMaleFloor().multiply(floor.getMagicPrice()));
        avgFemaleFloorUsd =
            avgFemaleFloorUsd.add(floor.getFemaleFloor().multiply(floor.getMagicPrice()));
        avgVroomFloorUsd =
            avgVroomFloorUsd.add(floor.getVroomFloor().multiply(floor.getMagicPrice()));
        avgBodyFloorUsd = avgBodyFloorUsd.add(floor.getBodyFloor().multiply(floor.getMagicPrice()));
        avgLandFloorUsd = avgLandFloorUsd.add(floor.getLandFloor().multiply(floor.getMagicPrice()));
      } else {
        // commit average to map
        maleFloors.put(
            floor.getCreated(),
            avgMaleFloor.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        femaleFloors.put(
            floor.getCreated(),
            avgFemaleFloor.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        vroomFloors.put(
            floor.getCreated(),
            avgVroomFloor.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        bodyFloors.put(
            floor.getCreated(),
            avgBodyFloor.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        landFloors.put(
            floor.getCreated(),
            avgLandFloor.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));

        maleFloorsUsd.put(
            floor.getCreated(),
            avgMaleFloorUsd.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        femaleFloorsUsd.put(
            floor.getCreated(),
            avgFemaleFloorUsd.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        vroomFloorsUsd.put(
            floor.getCreated(),
            avgVroomFloorUsd.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        bodyFloorsUsd.put(
            floor.getCreated(),
            avgBodyFloorUsd.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));
        landFloorsUsd.put(
            floor.getCreated(),
            avgLandFloorUsd.divide(new BigDecimal(hourlyFloors), RoundingMode.HALF_UP));

        avgMaleFloor = BigDecimal.ZERO;
        avgFemaleFloor = BigDecimal.ZERO;
        avgVroomFloor = BigDecimal.ZERO;
        avgBodyFloor = BigDecimal.ZERO;
        avgLandFloor = BigDecimal.ZERO;

        avgMaleFloorUsd = BigDecimal.ZERO;
        avgFemaleFloorUsd = BigDecimal.ZERO;
        avgVroomFloorUsd = BigDecimal.ZERO;
        avgBodyFloorUsd = BigDecimal.ZERO;
        avgLandFloorUsd = BigDecimal.ZERO;

        hourlyFloors = 0;

        lastHour = hour;
      }
    }

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    TimeSeries maleSeries = new TimeSeries("MALE");
    TimeSeries femaleSeries = new TimeSeries("LADY");
    TimeSeries vroomSeries = new TimeSeries("VROOM");
    TimeSeries bodySeries = new TimeSeries("SWOL");
    TimeSeries landSeries = new TimeSeries("LAND");

    for (Map.Entry<Date, BigDecimal> entry : maleFloors.entrySet()) {
      maleSeries.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : femaleFloors.entrySet()) {
      femaleSeries.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : vroomFloors.entrySet()) {
      vroomSeries.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : bodyFloors.entrySet()) {
      bodySeries.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : landFloors.entrySet()) {
      landSeries.add(new Hour(entry.getKey()), entry.getValue());
    }

    dataset.addSeries(maleSeries);
    dataset.addSeries(femaleSeries);
    dataset.addSeries(vroomSeries);
    dataset.addSeries(bodySeries);
    dataset.addSeries(landSeries);

    JFreeChart chart =
        ChartFactory.createTimeSeriesChart("Smolverse Floor", "wen", "MAGIC", dataset);

    final var theme = createSmoliverseTheme();
    theme.apply(chart);

    chart.getTitle().setPaint(new Color(107, 33, 168));

    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(new Color(11, 10, 36));
    plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    plot.setDomainCrosshairVisible(true);
    plot.setRangeCrosshairVisible(true);

    XYItemRenderer r = plot.getRenderer();
    if (r instanceof XYLineAndShapeRenderer renderer) {
      renderer.setDefaultShapesVisible(true);
      renderer.setDefaultShapesFilled(true);
      renderer.setDrawSeriesLineAsPath(true);
    }

    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat("HH:00"));

    final var img = chart.createBufferedImage(800, 300);

    BufferedImage output =
        new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = output.createGraphics();
    graphics.setComposite(AlphaComposite.SrcOver);
    graphics.drawImage(img, 0, 0, null);
    graphics.drawImage(imageSmol, 490, 2, null);
    graphics.dispose();

    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(output, "png", outputStream);
      currentFloorImageBytes = outputStream.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }

    TimeSeriesCollection datasetUsd = new TimeSeriesCollection();
    TimeSeries maleSeriesUsd = new TimeSeries("MALE");
    TimeSeries femaleSeriesUsd = new TimeSeries("LADY");
    TimeSeries vroomSeriesUsd = new TimeSeries("VROOM");
    TimeSeries bodySeriesUsd = new TimeSeries("SWOL");
    TimeSeries landSeriesUsd = new TimeSeries("LAND");

    for (Map.Entry<Date, BigDecimal> entry : maleFloorsUsd.entrySet()) {
      maleSeriesUsd.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : femaleFloorsUsd.entrySet()) {
      femaleSeriesUsd.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : vroomFloorsUsd.entrySet()) {
      vroomSeriesUsd.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : bodyFloorsUsd.entrySet()) {
      bodySeriesUsd.add(new Hour(entry.getKey()), entry.getValue());
    }

    for (Map.Entry<Date, BigDecimal> entry : landFloorsUsd.entrySet()) {
      landSeriesUsd.add(new Hour(entry.getKey()), entry.getValue());
    }

    datasetUsd.addSeries(maleSeriesUsd);
    datasetUsd.addSeries(femaleSeriesUsd);
    datasetUsd.addSeries(vroomSeriesUsd);
    datasetUsd.addSeries(bodySeriesUsd);
    datasetUsd.addSeries(landSeriesUsd);

    JFreeChart chartUsd =
        ChartFactory.createTimeSeriesChart("Smolverse Floor", "wen", "USD $", datasetUsd);

    theme.apply(chartUsd);

    chartUsd.getTitle().setPaint(new Color(107, 33, 168));

    plot = (XYPlot) chartUsd.getPlot();
    plot.setBackgroundPaint(new Color(11, 10, 36));
    plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    plot.setDomainCrosshairVisible(true);
    plot.setRangeCrosshairVisible(true);

    r = plot.getRenderer();
    if (r instanceof XYLineAndShapeRenderer renderer) {
      renderer.setDefaultShapesVisible(true);
      renderer.setDefaultShapesFilled(true);
      renderer.setDrawSeriesLineAsPath(true);
    }

    axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat("HH:00"));

    final var imgUsd = chartUsd.createBufferedImage(800, 300);

    output = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
    graphics = output.createGraphics();
    graphics.setComposite(AlphaComposite.SrcOver);
    graphics.drawImage(imgUsd, 0, 0, null);
    graphics.drawImage(imageSmol, 490, 2, null);
    graphics.dispose();

    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(output, "png", outputStream);
      currentFloorUsdImageBytes = outputStream.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static ChartTheme createSmoliverseTheme() {
    StandardChartTheme theme = new StandardChartTheme("Smoliverse");
    theme.setTitlePaint(Color.WHITE);
    theme.setSubtitlePaint(Color.WHITE);
    theme.setLegendBackgroundPaint(bgColor);
    theme.setLegendItemPaint(Color.WHITE);
    theme.setRegularFont(new Font("Quantico", Font.PLAIN, 12));
    theme.setSmallFont(new Font("Quantico", Font.PLAIN, 8));
    theme.setLargeFont(new Font("Quantico", Font.PLAIN, 20));
    theme.setExtraLargeFont(new Font("Quantico", Font.PLAIN, 24));
    theme.setChartBackgroundPaint(bgColor);
    theme.setPlotBackgroundPaint(bgColor);
    theme.setPlotOutlinePaint(Color.YELLOW);
    theme.setBaselinePaint(Color.WHITE);
    theme.setCrosshairPaint(Color.RED);
    theme.setLabelLinkPaint(Color.LIGHT_GRAY);
    theme.setTickLabelPaint(Color.WHITE);
    theme.setAxisLabelPaint(Color.WHITE);
    theme.setShadowPaint(Color.DARK_GRAY);
    theme.setItemLabelPaint(Color.WHITE);
    theme.setDrawingSupplier(
        new DefaultDrawingSupplier(
            new Paint[] {
              Color.decode("0xFFFF00"),
              Color.decode("0x0036CC"),
              Color.decode("0xFF0000"),
              Color.decode("0xFFFF7F"),
              Color.decode("0x6681CC"),
              Color.decode("0xFF7F7F"),
              Color.decode("0xFFFFBF"),
              Color.decode("0x99A6CC"),
              Color.decode("0xFFBFBF"),
              Color.decode("0xA9A938"),
              Color.decode("0x2D4587")
            },
            new Paint[] {Color.decode("0xFFFF00"), Color.decode("0x0036CC")},
            new Stroke[] {new BasicStroke(2.0f)},
            new Stroke[] {new BasicStroke(0.5f)},
            DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
    theme.setErrorIndicatorPaint(Color.LIGHT_GRAY);
    theme.setGridBandPaint(new Color(255, 255, 255, 20));
    theme.setGridBandAlternatePaint(new Color(255, 255, 255, 40));
    return theme;
  }
}
