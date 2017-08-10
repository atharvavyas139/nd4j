package org.nd4j.autodiff.functions;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.Getter;
import org.nd4j.autodiff.AbstractIdentityFactory;
import org.nd4j.autodiff.ArrayFactory;
import org.nd4j.autodiff.ArrayField;
import org.nd4j.autodiff.Field;
import org.nd4j.autodiff.samediff.SDGraph;
import org.nd4j.autodiff.samediff.SameDiff;

@Data
public class Variable<X extends Field<X>> extends DifferentialFunction<X> {
    @Getter
    private X m_x;
    private String m_name;
    private PreEvaluator<X> preEvaluator;

    protected Variable(SameDiff sameDiff,
                       String i_name,
                       X i_v) {
        this(sameDiff,i_name, i_v, null);
     }

    protected Variable(SameDiff sameDiff,
                       String i_name,
                       X i_v,PreEvaluator<X> preEvaluator) {
        super(sameDiff,null);
        this.preEvaluator = preEvaluator;
        setName(i_name);
        if (i_v != null) {
            m_x = i_v;
        } else {
            throw new IllegalArgumentException("Input not null value.");
        }

        if(i_v instanceof ArrayField) {
            ArrayField arrayField = (ArrayField) i_v;
            this.vertexId = arrayField.getVertex().vertexID();
        }

    }


    /**
     * Get the value specifying
     * whether to freeze the graph or not
     * @param freeze whether to freeze the graph or not,
     *               this means whether to add nodes to the internal
     *               computation graph or not
     * @return the value of this function
     */
    @Override
    public  X getValue(boolean freeze) {
        if(freeze) {
            return m_x;
        }

        return super.getValue(freeze);
    }

    private void setName(String i_name) {
        if (i_name != null) {
            m_name = i_name;// new String(i_name);
        } else {
            throw new IllegalArgumentException("Input not null value.");
        }
    }

    public String getName() {
        return m_name;
    }

    public void set(X i_v) {
        if (i_v != null) {
            m_x = i_v;
        } else {
            throw new IllegalArgumentException("Input not null value.");
        }
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public X doGetValue() {
        if (preEvaluator != null) {
            preEvaluator.update(this);
        }
        return m_x;
    }

    @Override
    public double getReal() {
        if (preEvaluator != null) {
            preEvaluator.update(this);
        }
        return m_x.getReal();
    }

    @Override
    public DifferentialFunction<X>[] args() {
        return new DifferentialFunction[] {this};
    }

    @Override
    public DifferentialFunction<X> arg() {
        return this;
    }

    @Override
    public Constant<X> diff(DifferentialFunction<X> i_v) {
        if(m_x instanceof ArrayField) {
            ArrayField arrayField = (ArrayField) m_x;
            Constant<X> ret =  (this.equals(i_v) ? new One<>(sameDiff,
                    arrayField.getInput().getShape()) : new Zero<>(sameDiff,
                    arrayField.getInput().getShape()));

            /*addEdges(graph,
                    this,ret,
                    "diff",
                    OpState.OpType.TRANSFORM,
                    arrayField.getInput().getShape());*/
            return ret;

        }

        throw new IllegalStateException("Illegal type for variable. Should be ArrayField");
    }


    /**
     * Get the result shape for this function
     * @return
     */
    @Override
    public int[] getResultShape() {
        ArrayField arrayField = (ArrayField) m_x;
        return arrayField.getInput().getShape();
    }


    @Override
    public String doGetFormula(List<Variable<X>> variables) {
        variables.add(this);
        return getName();
    }

    @Override
    public String functionName() {
        return m_name;
    }

    @Override
    public DifferentialFunction<X> div(DifferentialFunction<X> i_v) {
        return (i_v == this) ? new One<>(sameDiff,i_v.getResultShape()) : super.mul(i_v.inverse());
    }

    @Override
    public DifferentialFunction<X> dup() {
        return new Variable<>(sameDiff, getName(),
                m_x);
    }

    @Override
    public String toString() {
        return "Variable{" +
                "m_name='" + m_name + '\'' +
                ", vertexId=" + vertexId +
                ", extraArgs=" + Arrays.toString(extraArgs) +
                '}';
    }
}
