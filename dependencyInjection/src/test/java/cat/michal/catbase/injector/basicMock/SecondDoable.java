package cat.michal.catbase.injector.basicMock;

import cat.michal.catbase.injector.annotations.Component;

@SuppressWarnings("unused")
@Component
public class SecondDoable implements Doable {
    @Override
    public int result() {
        return 0;
    }
}
