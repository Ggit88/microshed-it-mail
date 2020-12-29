package it.gf.learning.ol.ioam.check;

import javax.enterprise.context.ApplicationScoped;

import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

import org.eclipse.microprofile.health.Liveness;

import it.gf.learning.ol.ioam.rest.OTPResource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Liveness
@ApplicationScoped
public class RetrieveOtpLivenessCheck implements HealthCheck 
{
    @Override
    public HealthCheckResponse call() 
    {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memBean.getHeapMemoryUsage().getUsed();
        long memMax = memBean.getHeapMemoryUsage().getMax();
        return HealthCheckResponse.named(
        		OTPResource.class.getSimpleName() + "Liveness")
                                  .withData("memory used", memUsed)
                                  .withData("memory max", memMax)
                                  .state(memUsed < memMax * 0.9).build();
    }
}
