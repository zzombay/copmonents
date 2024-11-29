package ru.greenatom.rest;

import de.mpdv.hydra.common.dataTypes.IRequestEnvironment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.greenatom.components.authentication.RestAuthenticationToken;
import ru.greenatom.rest.service.RequestExecutionServiceList;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
public class RestControllerExecution {
    @RequestMapping(value = "/{instanceId:}/{service:.+}",
                    produces = {"application/json"},
                    method = RequestMethod.GET)
    public List<Map<String, Object>> getRestRequest(RestAuthenticationToken auth, @PathVariable int instanceId, @PathVariable String service) {
        IRequestEnvironment env = auth.getEnv();
        return (List<Map<String, Object>>) RequestExecutionServiceList.executeRequest(env, service);
    }
}
