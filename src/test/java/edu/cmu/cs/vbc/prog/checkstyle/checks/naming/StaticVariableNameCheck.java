////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package edu.cmu.cs.vbc.prog.checkstyle.checks.naming;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.ScopeUtils;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

/**
 * <p>
 * Checks that static, non-final variable names conform to a format specified
 * by the format property. The format is a
 * {@link java.util.regex.Pattern regular expression} and defaults to
 * <strong>^[a-z][a-zA-Z0-9]*$</strong>.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="StaticVariableName"/&gt;
 * </pre>
 * <p>
 * An example of how to configure the check for names that begin with
 * a lower case letter, followed by letters, digits, and underscores is:
 * </p>
 * <pre>
 * &lt;module name="StaticVariableName"&gt;
 *    &lt;property name="format" value="^[a-z](_?[a-zA-Z0-9]+)*$"/&gt;
 * &lt;/module&gt;
 * </pre>
 * @author Rick Giles
 * @version 1.0
 */
public class StaticVariableNameCheck
    extends AbstractAccessControlNameCheck
{
	
	@VConditional
	private static boolean StaticVariableName = true;
	
	@Override
	public boolean isEnabled() {
		return StaticVariableName;
	}
	
    /** Creates a new <code>StaticVariableNameCheck</code> instance. */
    public StaticVariableNameCheck()
    {
        super("^[a-z][a-zA-Z0-9]*$");
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.VARIABLE_DEF};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {TokenTypes.VARIABLE_DEF};
    }

    @Override
    protected final boolean mustCheckName(DetailAST ast)
    {
        final DetailAST modifiersAST =
            ast.findFirstToken(TokenTypes.MODIFIERS);
        final boolean isStatic = (modifiersAST != null)
            && modifiersAST.branchContains(TokenTypes.LITERAL_STATIC);
        final boolean isFinal = (modifiersAST != null)
            && modifiersAST.branchContains(TokenTypes.FINAL);

        return (isStatic
                && !isFinal
                && shouldCheckInScope(modifiersAST)
                && !ScopeUtils.inInterfaceOrAnnotationBlock(ast));
    }
}