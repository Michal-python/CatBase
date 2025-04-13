package cat.michal.catbase.injector.basicMock;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Primary;

@Primary
@Component
public class FirstDoable implements Doable {
    @Override
    public int result() {
        return 1;
    }
}
