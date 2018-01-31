package org.lockss.laaws.poller;

import static org.lockss.app.LockssApp.PARAM_START_PLUGINS;
import static org.lockss.app.ManagerDescs.ACCOUNT_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.CRAWL_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.HASH_SERVICE_DESC;
import static org.lockss.app.ManagerDescs.IDENTITY_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.METADATA_DB_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.METADATA_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.NODE_MANAGER_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.PLUGIN_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.POLL_MANAGER_DESC;
import static org.lockss.app.ManagerDescs.ROUTER_MANAGER_DESC;

import javax.jms.ConnectionFactory;
import org.lockss.app.LockssApp;
import org.lockss.app.LockssApp.AppSpec;
import org.lockss.app.LockssApp.ManagerDesc;
import org.lockss.app.LockssDaemon;
import org.lockss.plugin.PluginManager;
import org.lockss.rs.base.BaseSpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = { "org.lockss.laaws.poller", "org.lockss.laaws.poller.api" })
public class PollerApplication extends BaseSpringBootApplication implements CommandLineRunner {
  private static final Logger logger =
      LoggerFactory.getLogger(PollerApplication.class);
  private static ConfigurableApplicationContext appContext;

  // Manager descriptors.  The order of this table determines the order in
  // which managers are initialized and started.
  private static final ManagerDesc[] myManagerDescs = {
      ACCOUNT_MANAGER_DESC,
      PLUGIN_MANAGER_DESC,
      CRAWL_MANAGER_DESC,
      // start database manager before any manager that uses it.
      METADATA_DB_MANAGER_DESC,
      // start metadata manager after pluggin manager and database manager.
      METADATA_MANAGER_DESC,
      IDENTITY_MANAGER_DESC,
      HASH_SERVICE_DESC,
      ROUTER_MANAGER_DESC,
      NODE_MANAGER_MANAGER_DESC, // remove this when we eliminate the node manager.
      POLL_MANAGER_DESC
  };

  @Bean
  public JmsListenerContainerFactory<?> pollMsgFactory(
      ConnectionFactory connectionFactory,
      DefaultJmsListenerContainerFactoryConfigurer configurer) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    // This provides all boot's default to this factory, including the message converter
    configurer.configure(factory, connectionFactory);
    // Override some of Boot's default here.
    return factory;
  }

  @Bean // Serialize message content to json using TextMessage
  public MessageConverter jacksonJmsMessageConverter() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    return converter;
  }

  @Override
  public void run(String... args) throws Exception {
    if (args != null && args.length > 0) {
      if (args[0].equals("exitcode")) {
        throw new ExitException();
      }
      else {
        logger.info("Starting the LOCKSS daemon");
        AppSpec spec = new AppSpec()
            .setName("Poller Service")
            .setArgs(args)
            .setAppManagers(myManagerDescs)
            .addAppConfig(PARAM_START_PLUGINS, "true")
            .addAppConfig(PluginManager.PARAM_START_ALL_AUS, "true");
        LockssApp.startStatic(LockssDaemon.class, spec);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    logger.info("Starting the  Poller REST service...");
    configure();
    appContext = SpringApplication.run(PollerApplication.class, args);
  }

  class ExitException extends RuntimeException implements ExitCodeGenerator {
    private static final long serialVersionUID = 1L;

    @Override
    public int getExitCode() {
      return 10;
    }

  }
}
