package cat.michal.catbase.injector.multipleInstances;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Primary;

@Primary
@Component
public class SecondInstance implements InstanceHolder {
}
