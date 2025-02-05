
package in.fortytwo42.adapter.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

// TODO: Auto-generated Javadoc

/**
 * The Class GraphiteReporterUtil.
 */
public class GraphiteReporterUtil {

    /**
     * The graphite reporter util log.
     */
    private static String GRAPHITE_REPORTER_UTIL_LOG = "<<<<< GraphiteReporterUtil";

    private static Logger logger= LogManager.getLogger(GraphiteReporterUtil.class);

    /**
     * The Constant PI_CONTROL_ADAPTER.
     */
    private static final String PI_CONTROL_ADAPTER = "Pi-control-adapter";

    /**
     * The graphite reporter util.
     */
    private static GraphiteReporterUtil graphiteReporterUtil;

    /**
     * The reporter.
     */
    private static GraphiteReporter reporter;

    /**
     * The reporter.
     */
    private static ConsoleReporter consoleReporter;

    /**
     * The metrics.
     */
    private static MetricRegistry metrics = new MetricRegistry();

    /**
     * The health status.
     */
    private static Counter healthStatus;

    /**
     * Instantiates a new graphite reporter util.
     */
    private GraphiteReporterUtil() {
        String prefixName;
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            prefixName = PI_CONTROL_ADAPTER + "-" + hostname;
        }
        catch (UnknownHostException e) {
            prefixName = PI_CONTROL_ADAPTER;
            logger.log(Level.FATAL, e);
        }
        Graphite graphite = new Graphite(Config.getInstance().getProperty(Constant.MONITORING_IP), Integer.parseInt(Config.getInstance().getProperty(Constant.MONITORING_PORT)));
        //Graphite graphite = new Graphite("172.31.4.254", 2003);
        reporter = GraphiteReporter.forRegistry(metrics).prefixedWith(prefixName).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).filter(MetricFilter.ALL).build(graphite);
        reporter.start(60, TimeUnit.SECONDS);

        /*consoleReporter =
                        ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
                                .filter(MetricFilter.ALL).build();
        consoleReporter.start(30, TimeUnit.SECONDS);*/
        System.out.println("Grahite reporter started successfully . ");
    }

    /**
     * Gets the single instance of GraphiteReporterUtil.
     *
     * @return single instance of GraphiteReporterUtil
     */
    public static GraphiteReporterUtil getInstance() {
        if (graphiteReporterUtil == null) {
            graphiteReporterUtil = new GraphiteReporterUtil();
        }
        return graphiteReporterUtil;
    }

    /**
     * Start reporting.
     */
    public void startReporting() {
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " startReporting : start");
        healthStatus = metrics.counter("health-status");
        healthStatus.inc();
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " startReporting : end");
    }

    /**
     * Stop reporting.
     */
    public void stopReporting() {
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " stopReporting : start");
        if (healthStatus == null) {
            healthStatus = metrics.counter("health-status");
        }
        healthStatus.dec();
        if (reporter != null) {
            reporter.report();
        }
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " stopReporting : end");
    }

    /**
     * Start AD sync timer.
     *
     * @return the timer. context
     */
    public Timer.Context startADSyncTimer() {
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " startADSyncTimer : start");
        Timer timer = metrics.timer("schedular.ADSync");
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " startADSyncTimer : end");
        return timer.time();
    }

    /**
     * Stop timer.
     *
     * @param context the context
     */
    public void stopTimer(Timer.Context context) {
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " stopTimer : start");
        context.stop();
        reporter.report();
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " stopTimer : end");
    }

    /**
     * Mark.
     */
    public void mark() {
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " mark : start");
        Meter requests = metrics.meter("API ");
        requests.mark();
        logger.log(Level.DEBUG, GRAPHITE_REPORTER_UTIL_LOG + " mark : end");
    }

    /**
     * Gets the metric registry.
     *
     * @return the metric registry
     */
    public MetricRegistry getMetricRegistry() {
        return metrics;
    }
}
