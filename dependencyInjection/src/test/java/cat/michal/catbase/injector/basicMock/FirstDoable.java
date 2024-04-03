package cat.michal.catbase.injector.basicMock;

import cat.michal.catbase.injector.annotations.Primary;

@Primary
public class FirstDoable implements Doable {
    @Override
    public int result() {
        return 1;
    }
}
