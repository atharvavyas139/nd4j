package org.nd4j.autodiff.functions;

import lombok.NoArgsConstructor;
import org.nd4j.autodiff.ArrayField;
import org.nd4j.autodiff.Field;
import org.nd4j.autodiff.graph.Graph;
import org.nd4j.autodiff.opstate.NDArrayInformation;
import org.nd4j.autodiff.opstate.OpState;
import org.nd4j.autodiff.samediff.SDGraph;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.shape.Shape;

import java.util.List;


/**
 * Created by agibsonccc on 4/12/17.
 */
@NoArgsConstructor
public abstract class AbstractBinaryReduceFunction<X extends  Field<ArrayField>> extends AbstractBinaryFunction<X> {
    protected int[] dimensions;


    public AbstractBinaryReduceFunction(SameDiff sameDiff,
                                        DifferentialFunction<ArrayField> i_v1,
                                        DifferentialFunction<ArrayField> i_v2,
                                        int...dimensions) {
        super(sameDiff, i_v1, i_v2);
        this.dimensions = dimensions;
        //note that the below won't trigger if dimensions are null
        //please don't remove this
        addEdges(sameDiff,i_v1,
                i_v2,functionName());
    }

    public AbstractBinaryReduceFunction(SameDiff sameDiff) {
        super(sameDiff);
    }


    @Override
    protected void addEdges(SameDiff sameDiff,
                            DifferentialFunction<ArrayField> i_v1,
                            DifferentialFunction<ArrayField> i_v2,
                            String opName) {
        if(i_v1.getValue(true) instanceof ArrayField) {
            ArrayField arrayField = i_v1.getValue(true);
            //skip empty dimensions
            if(dimensions == null)
                return;
            addEdges(sameDiff,i_v1,i_v2,opName,
                    OpState.OpType.ACCUMULATION,
                    Shape.getReducedShape(arrayField.getInput().getShape(),
                            dimensions));

        }

        else
            throw new UnsupportedOperationException("Only supporting array fields");
    }

    @Override
    public String doGetFormula(List<Variable<ArrayField>> variables) {
        return toString();
    }

    @Override
    public double getReal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return functionName() + "(" + larg() + "," + rarg() + ")";
    }


    @Override
    public DifferentialFunction<ArrayField> dup() {
        try {
            return getClass().getConstructor(sameDiff.getClass(),larg()
                    .getClass(),rarg().getClass()).newInstance(sameDiff,larg(),rarg());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
