package rest.iot.itest;

import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.remote.RBCRemoteTargetOptions;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.systemTimeout;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class RESTIntegrationTest {

	private static final boolean DEBUG_HOLD = false;
	private static final String DEBUG_PORT = "5005";
	private static final boolean RUN_EMBEDDED = false;
	private static final String OUTER_USER_DIR_PROPERTY = "org.ops4j.pax.source.user.dir";

	@Inject
	private static BundleContext bcontext;

	@Before
	public void load() throws Exception {

	}

	@Test
	public void integrationTest() throws Exception {
		assertNotNull(bcontext);
		assertNotNull(waitForReference(bcontext, HTTPConduitConfigurer.class,null,5000L));
	}

	@Configuration
	public Option[] config() throws Exception {

		deleteNotEmptyFolder("target", "exam");

		MavenArtifactUrlReference karaf = maven()
				.groupId("org.apache.karaf")
				.artifactId("apache-karaf")
				.version("4.2.11")
				.type("tar.gz");

		MavenArtifactUrlReference standardRepo = maven()
				.groupId("org.apache.karaf.features")
				.artifactId("standard")
				.version("4.2.11")
				.classifier("features")
				.type("xml");

		MavenArtifactUrlReference sutRepo = maven()
				.groupId("com.flairbit.iot")
				.artifactId("features")
				.version("2.1.27-SNAPSHOT")
				.classifier("features")
				.type("xml");

		return new Option[] {
				CoreOptions.composite(configFiles("src/test/resources/karaf/etc/", true)),
				editConfigurationFilePut( "etc/config.properties", "felix.fileinstall.poll", String.valueOf(Integer.MAX_VALUE)),

				karafDistributionConfiguration()
						.frameworkUrl(karaf)
						.unpackDirectory(new File("target", "exam"))
						.runEmbedded(RUN_EMBEDDED)
						// See https://ops4j1.jira.com/browse/PAXEXAM-554:
						// Always set useDeployFolder(false). This is also recommended in the pax exam karaf user guide.
						.useDeployFolder(false),
				// This gives a fast fail when any bundle is unresolved:
				systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
				logLevel(LogLevel.valueOf("INFO")),
				keepRuntimeFolder(),
				configureConsole().ignoreLocalConsole().startRemoteShell(),
				systemProperty(OUTER_USER_DIR_PROPERTY).value(System.getProperty("user.dir")),
				systemProperty("org.ops4j.pax.exam.raw.extender.intern.Parser.DEFAULT_TIMEOUT").value("300000"),
				systemProperty("org.ops4j.pax.exam.invoker.junit.internal.JUnitProbeInvokerFactory.DEFAULT_TIMEOUT").value("300000"),
				systemTimeout(300000L),
				RBCRemoteTargetOptions.waitForRBCFor((int) 300000L),
				KarafDistributionOption.debugConfiguration(DEBUG_PORT, DEBUG_HOLD),

				features(standardRepo , "scr"),
				features(sutRepo,"flairkit-test")
		};
	}

	private void deleteNotEmptyFolder(String first, String... more) {
		if(Paths.get(first, more).toFile().exists()) {
			try {
				Files.list(Paths.get(".", "target", "exam"))
						.filter(p -> p.toFile().isDirectory())
						.forEach(el->deleteDirectory(el.toFile()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Option[] configFiles(final String path, boolean resolveRelativePath) throws Exception {
		final String bp;
		List<Option> options = new ArrayList<>();
		if(!path.startsWith(File.separator))
			bp = File.separator + path;
		else
			bp = path;
		URI uri;
		if (resolveRelativePath) {
			uri = getURI(bp);
		} else {
			uri = URI.create("file:" + bp);
		}
		if (Files.exists(Paths.get(uri), LinkOption.NOFOLLOW_LINKS) &&
				Paths.get(uri).toFile().isDirectory()) {
			Iterator<Path> iter = Files.list(Paths.get(uri)).iterator();
			while(iter.hasNext()){
				Path f = iter.next();
				if( !f.toFile().isDirectory()) {
					String fileName = f.getFileName().toString();
					options.add(
						replaceConfigurationFile(
								"etc" + File.separator + fileName,
								f.toFile()));
				}
			}
		} else {
			options.add(
					replaceConfigurationFile(
						"etc" + File.separator + Paths.get(uri).toFile().getName(),
						Paths.get(uri).toFile()));
		}
		return options.toArray(new Option[]{});
	}

	private boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	private URI getURI(String relativePath) {
		String userdir = PathUtils.getBaseDir();
		if(userdir == null || "".equals(userdir)) {
			userdir = System.getProperty("karaf.base");
		}
		if(relativePath == null)
			relativePath = "";
		return URI.create("file:" + userdir + relativePath);
	}

	private <T> T waitForReference(BundleContext context, Class<T> clazz, String strFilter, long timeout) throws Exception {
		ServiceTracker<T,?> st;
		if (strFilter != null) {
			Filter filter = context.createFilter(strFilter);
			st = new ServiceTracker<>(context, filter, null);
		} else {
			st = new ServiceTracker<>(context, clazz, null);
		}
		st.open();
		return clazz.cast(st.waitForService(timeout));
	}
}
