package org.emoflon.ibex.tgg.operational.csp.constraints;

import org.emoflon.ibex.tgg.operational.csp.RuntimeTGGAttributeConstraint;
import org.emoflon.ibex.tgg.operational.csp.RuntimeTGGAttributeConstraintVariable;
import org.emoflon.ibex.tgg.operational.csp.generator.Generator;

public class SetDefaultString extends RuntimeTGGAttributeConstraint
{
   public void solve(RuntimeTGGAttributeConstraintVariable var_0, RuntimeTGGAttributeConstraintVariable var_1)
   {
      String bindingStates = getBindingStates(var_0, var_1);

      switch (bindingStates)
      {
      case "BB":
         setSatisfied(true);
         return;
      case "FB":
         var_0.bindToValue(var_1.getValue());
         setSatisfied(true);
         return;

      case "FF":

         var_0.bindToValue(Generator.getNewRandomString(var_0.getType()));
         var_1.bindToValue(Generator.getNewRandomString(var_1.getType()));
         setSatisfied(true);
         return;

      default:
         throw new UnsupportedOperationException("This case in the constraint has not been implemented yet: " + bindingStates);
      }

   }
}