package org.nthdimenzion.cqrs.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static com.google.common.collect.Maps.*;

@Component
public class SpringBasedCommandHandlerRegistry implements ICommandHandlerRegistry, ApplicationContextAware, DestructionAwareBeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpringBasedCommandHandlerRegistry.class);

    protected ApplicationContext applicationContext;
    private Map<Class<? extends ICommand>, String> commandTypeToCommandHandler = newConcurrentMap();



    @Override
    public ICommandHandler findCommandHanlerFor(Class<? extends ICommand> commandType) {
        String commandHandlerName = commandTypeToCommandHandler.get(commandType);
        return applicationContext.getBean(commandHandlerName,ICommandHandler.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
    commandTypeToCommandHandler.clear();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        if (isCommandHandlerSubclass(clazz)){
            logger.debug("Entry postProcessAfterInitialization " + beanName);
            logger.debug("Command name is " + getHandledCommandType(clazz).getSimpleName());
            Class<? extends ICommand> commandType = (Class<? extends ICommand>) getHandledCommandType(clazz);
            commandTypeToCommandHandler.put(commandType,beanName);
        }
        return bean;
    }

    private boolean isCommandHandlerSubclass(Class<?> beanClass) {
        return ICommandHandler.class.isAssignableFrom(beanClass);
    }



    private Class<?> getHandledCommandType(Class<?> clazz) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        ParameterizedType type = findByRawType(genericInterfaces, ICommandHandler.class);
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    private ParameterizedType findByRawType(Type[] genericInterfaces, Class<?> expectedRawType) {
        for (Type type : genericInterfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parametrized = (ParameterizedType) type;
                if (expectedRawType.equals(parametrized.getRawType())) {
                    return parametrized;
                }
            }
        }
        throw new RuntimeException();
    }
}
