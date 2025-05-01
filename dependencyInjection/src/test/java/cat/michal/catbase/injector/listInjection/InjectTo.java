package cat.michal.catbase.injector.listInjection;

import cat.michal.catbase.injector.annotations.Component;
import cat.michal.catbase.injector.annotations.Inject;

import java.util.List;

@Component
public class InjectTo {
    @Inject
    private List<AbstractionLayer> layers;
    @Inject("first")
    private AbstractionLayer firstLayer;

    private final AbstractionLayer secondLayer;

    private List<SecondAbstraction> abstractions;

    public InjectTo(@Inject("second") AbstractionLayer secondLayer, List<SecondAbstraction> abstractions) {
        this.secondLayer = secondLayer;
        this.abstractions = abstractions;
    }

    public List<SecondAbstraction> getAbstractions() {
        return abstractions;
    }

    public AbstractionLayer getSecondLayer() {
        return secondLayer;
    }

    public AbstractionLayer getFirstLayer() {
        return firstLayer;
    }

    public List<AbstractionLayer> getLayers() {
        return layers;
    }
}
