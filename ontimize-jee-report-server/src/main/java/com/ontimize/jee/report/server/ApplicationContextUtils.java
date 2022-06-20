package com.ontimize.jee.report.server;

import com.ontimize.jee.server.rest.ORestController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationContextUtils implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private static ApplicationContextUtils instance;
    
    private ApplicationContextUtils() {
        //no-op
    }
    
    private static ApplicationContextUtils getInstance() {
        if(instance == null) {
           instance = new ApplicationContextUtils(); 
        }
        return instance;
    }
    
    public Object getServiceBean(final String servicePath, final String serviceName) {

//        String[] beanNamesForType = applicationContext.getBeanNamesForType(ORestController.class);
//        List<String> restController = Stream.of(beanNamesForType).filter((item) -> item.startsWith(serviceName.toLowerCase()))
//                .collect(Collectors.toList());

        //Method 1
        if(!StringUtils.isBlank(servicePath)) {
            RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                    .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> requestMap = requestMappingHandlerMapping.getHandlerMethods();

            List<HandlerMethod> requestMapHandlerMethodList = requestMap.keySet().stream()
                    .filter(key -> key.getActivePatternsCondition().toString().equals("[" + servicePath + "/{name}/search]"))
                    .map(requestMap::get)
                    .collect(Collectors.toList());

            if (requestMapHandlerMethodList.size() == 1) {
                Class<?> restControllerBeanName = requestMapHandlerMethodList.get(0).getBeanType();
                Object restControllerBean = applicationContext.getBean(restControllerBeanName);
                if (restControllerBean instanceof ORestController) {
                    Object service1 = ((ORestController<?>) restControllerBean).getService();
                }
            }
        }
        
        return null;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        
    }
}
