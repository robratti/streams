package my.examples.apache.streams.factory;

import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Meter;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

import java.util.concurrent.CompletableFuture;

public abstract class MetersFactoryImpl <T extends Meter> implements MetersFactory<T> {
    private final Class<T> meterClass;
    protected ApplicationContext applicationContext;

    protected MetersFactoryImpl(Class<T> meterClass) {
        this.meterClass = meterClass;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized CompletableFuture<T> getMeter(String meterName) throws BeansException {
        var meter = this.getBeanInstance(meterName);
        return CompletableFuture.supplyAsync(() -> meter);
    }

    @VisibleForTesting
    protected T getBeanInstance(String beanName) {
        var meterBean = applicationContext.getBean(beanName);
        if (meterClass.isInstance(meterBean)) {
            return meterClass.cast(meterBean);
        }

        throw new BeanInstantiationException(meterBean.getClass(), beanName);
    }
}
