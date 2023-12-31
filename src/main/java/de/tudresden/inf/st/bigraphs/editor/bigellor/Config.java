package de.tudresden.inf.st.bigraphs.editor.bigellor;

import org.bigraphs.spring.data.cdo.CdoClient;
import org.bigraphs.spring.data.cdo.CdoServerConnectionString;
import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.bigraphs.spring.data.cdo.SimpleCdoDbFactory;
import org.bigraphs.spring.data.cdo.repository.config.EnableCdoRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * Spring bean configuration for the CDO repository.
 * <p>
 * Change here the CDO server details if necessary.
 */
@Configuration
@EnableAsync
@EnableCdoRepositories
public class Config {

    CdoServerConnectionString cdoServerConnectionStr; // = new CdoServerConnectionString("cdo://localhost:2036/repo1");

    @Value("${bigellor.cdo.server}")
    public String propertyCdoServerAddress;
    @Value("${bigellor.cdo.server.port}")
    public String propertyCdoServerPort;
    @Value("${bigellor.cdo.repo}")
    public String propertyCdoRepository;

    @PostConstruct
    protected void init() {
        String connection = String.format(
                "cdo://%s:%s/%s",
                propertyCdoServerAddress,
                propertyCdoServerPort,
                propertyCdoRepository
        );
        cdoServerConnectionStr = new CdoServerConnectionString(connection);
    }

    @Bean
    public CdoTemplate cdoTemplate() throws Exception {
        return new CdoTemplate(new SimpleCdoDbFactory(cdoClient(), cdoServerConnectionStr.getRepoName()));
    }

    @Bean
    public CdoClient cdoClient() {
        return new CdoClient(cdoServerConnectionStr.getServer(), cdoServerConnectionStr.getPort());
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("CDOServerAsync-");
        executor.initialize();
        return executor;
    }

//    @Bean
//    public BeforeConvertCallback<NewProjectDTO> beforeConvertCallback() {
//
//        return (newProjectDTO) -> {
//            if (newProjectDTO.getNewProjectId() == -1) {
//                newProjectDTO.setNewProjectId(1+1);
//            }
//            return newProjectDTO;
//        };
//    }
}
