package org.nd4j.autodiff.functions;

import org.nd4j.autodiff.ArrayField;
import org.nd4j.autodiff.Field;
import org.nd4j.autodiff.opstate.OpState;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.ops.impl.transforms.arithmetic.MulOp;


public class One<X extends Field<X>> extends Constant<X> {


    public One(SameDiff sameDiff,
               int[] shape) {
        super(sameDiff, (X) sameDiff.getArrayFactory().one(shape),shape);
        this.shape = shape;
        ArrayField arrayField = (ArrayField) m_x;
        arrayField.getInput().setScalarValue(1.0);
    }




    @Override
    public DifferentialFunction<X> mul(DifferentialFunction<X> i_v) {
        DifferentialFunction<X> dup = i_v.dup();
        if(i_v.getValue(true) instanceof ArrayField) {
            ArrayField arrayField = (ArrayField) i_v.getValue(true);
            addEdges(sameDiff,
                    dup,
                    this,
                    new MulOp().name(),
                    OpState.OpType.TRANSFORM,
                    arrayField.getInput().getShape());
        }

        return dup;
    }



    @Override
    public DifferentialFunction<X> dup() {
        return new One<>(sameDiff, shape);
    }
}
