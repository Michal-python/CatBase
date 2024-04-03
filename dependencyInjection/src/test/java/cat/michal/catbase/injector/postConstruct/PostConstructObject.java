package cat.michal.catbase.injector.postConstruct;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.PostConstruct;

@Component
public class PostConstructObject {
    private String field;

    @PostConstruct
    @SuppressWarnings("unused")
    public void method() {
        field = "Value!";
    }

    public String getField() {
        return field;
    }
}
