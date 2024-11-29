package ru.greenatom.rest.service;

import de.mpdv.hydra.common.dataTypes.IRequestEnvironment;
import de.mpdv.sdi.data.ColumnConfigurator;
import de.mpdv.sdi.data.DataTableColumnInfo;
import de.mpdv.sdi.data.OperatorType;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.systemutility.*;
import org.springframework.stereotype.Service;
import ru.greenatom.hydra.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestExecutionServiceList {
    private static IRequestEnvironment environment;

    public static List<?> executeRequest(IRequestEnvironment env, String serviceName) {
        environment = env;
        ISystemUtilFactoryFactory factoryFactory = new SystemUtilFactoryFactory(env);
        ISystemUtilFactory factory = factoryFactory.createSystemUtilFactory();
        callSessionService(factory, SessionOption.LOGIN);
        List<?> listResult = new ArrayList<>();
        try {
            IServiceConfigProvider serviceConfigProvider = factory.fetchUtil("ServiceConfigProvider");
            IServiceConfig serviceConfig = serviceConfigProvider.fetchServiceConfig(serviceName);
            List<String> columnList = serviceConfig.getStringValues("/service/parameter/@Acronym");
            IDataTable dataTable = ((IServiceCaller) factory.fetchUtil("ServiceCaller"))
                    .callService(serviceName, new ColumnConfigurator(false, columnList)
                            , Collections.emptyList(), Collections.emptyList()).getResultTable();
            listResult = dataTable.getData().parallelStream().map(row ->
                    dataTable.getMetadata().values().parallelStream().collect(HashMap::new, (HashMap<String, Object> map, DataTableColumnInfo dataTableColumnInfo) ->
                            map.put(dataTableColumnInfo.getName(), row.get(dataTableColumnInfo.getIndex())), HashMap::putAll)).collect(Collectors.toList());
        } catch (Exception e) {
            // ignore
        } finally {
            callSessionService(factory, SessionOption.LOGOUT);
        }
        return listResult;
    }

    private static void callSessionService(ISystemUtilFactory factory, SessionOption sessionOption) {
        ServiceUtils.callService(factory, "SYSSessionLifeCycle" + sessionOption.getOption(), ((specialParams, filterParams) -> {
            specialParams.add(new SpecialParam("license.token.id", OperatorType.EQUAL, environment.getLicenseTokenId()));
            specialParams.add(new SpecialParam("session.id", OperatorType.EQUAL, environment.getSessionId()));
        }));
    }

    enum SessionOption {
        LOGIN("login"),
        LOGOUT("logout");

        private final String option;

        public String getOption() {
            return option;
        }

        private SessionOption(String option) {
            this.option = option;
        }


    }
}
