package de.timmi6790.external_modules.mineplexstats.commands.java;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaBoard;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaRatioPlayer;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaStat;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.PieSeries;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;

public class JavaPlayerStatsRatioCommand extends AbstractJavaStatsCommand {
    public JavaPlayerStatsRatioCommand() {
        super("playerstats", "Player stats as graph", "<player> <stat> [board]", "pls", "plsats", "plstat");

        this.setCategory("PROTOTYPE - MineplexStats - Java");
        this.setDefaultPerms(true);
        this.setMinArgs(2);

        this.addExampleCommands(
                "nwang888 wins",
                "nwang888 wins yearly"
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Parse args
        final String player = this.getPlayer(commandParameters, 0);
        // TODO: Add a parser
        final JavaStat stat = this.getStat(commandParameters, 1);
        final JavaBoard board = this.getBoard(stat, commandParameters, 2);
        final long unixTime = Instant.now().getEpochSecond();// this.getUnixTime(commandParameters, 3);

        // Web request
        final ResponseModel responseModel = this.getStatsModule().getMpStatsRestClient().getPlayerStatsRatio(player, stat.getPrintName(), board.getName(), unixTime);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final JavaRatioPlayer javaRatioPlayer = (JavaRatioPlayer) responseModel;

        // Add to pie chart
        // TODO: Write own piechart
        // https://cdn.discordapp.com/attachments/677989190914408478/716754040352145518/unknown.png

        final PieChart chart = new PieChartBuilder()
                .width(1000)
                .height(800)
                .title(((JavaRatioPlayer) responseModel).getInfo().getName() + " " + stat.getPrintName() + " " + board.getName() + " " + this.getFormattedUnixTime(unixTime))
                .theme(Styler.ChartTheme.GGPlot2)
                .build();

        final PieStyler styler = chart.getStyler();
        styler.setLegendVisible(false);

        styler.setPlotBackgroundColor(new Color(54, 57, 63));
        styler.setChartBackgroundColor(new Color(54, 57, 63));
        styler.setChartFontColor(Color.WHITE);
        styler.setChartTitleFont(new Font("Arial", Font.PLAIN, 38));
        styler.setChartTitleBoxVisible(false);

        styler.setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Pie);
        styler.setStartAngleInDegrees(110);

        styler.setShowTotalAnnotations(true);
        styler.setDrawAllAnnotations(true);
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        styler.setAnnotationsFont(new Font("Arial", Font.PLAIN, 22));
        styler.setAnnotationsFontColor(Color.BLACK);

        styler.setSumVisible(true);
        styler.setSumFont(new Font("Arial", Font.PLAIN, 30));

        styler.setCircular(false);

        javaRatioPlayer.getStats().values()
                .stream()
                .sorted(Comparator.comparingLong(JavaRatioPlayer.Stat::getScore))
                .forEach(value -> chart.addSeries(value.getGame(), value.getScore()));

        // Send to server
        Optional<InputStream> inputStream;
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", os);
            inputStream = Optional.of(new ByteArrayInputStream(os.toByteArray()));
        } catch (final IOException e) {
            e.printStackTrace();
            inputStream = Optional.empty();
        }

         /*

        final PicturePie picturePie = new PicturePie(
                javaRatioPlayer.getStats().values()
                        .stream()
                        .sorted(Comparator.comparingLong(JavaRatioPlayer.Stat::getScore))
                        .map(value -> new PicturePie.Slice(value.getGame(), value.getScore()))
                        .toArray(PicturePie.Slice[]::new)
        );
        
        final Optional<InputStream> inputStream = picturePie.getPie();

          */

        final CommandResult commandResult = this.sendPicture(
                commandParameters,
                inputStream,
                ((JavaRatioPlayer) responseModel).getInfo().getName() + "-" + stat.getName() + "-" + board.getName() + "-" + unixTime
        );


        commandParameters.getDiscordChannel()
                .sendMessage(this.getEmbedBuilder(commandParameters)
                        .setTitle("Prototype Command")
                        .setDescription("Thx for using this prototype command. \n" +
                                "This command is clearly not done and all the data is based on the leaderboards, but have fun with the limited version of this command.")
                        .build())
                .queue();
        return commandResult;
    }
}
