package madlang;

import java.util.Arrays;

public class Main {
	static int testsPassed = 0;
	static int testsFailed = 0;

	// Helper to check a result equals expected
	static void assertEqual(String testName, Object result, Object expected) {
			if (expected.equals(result)) {
					System.out.println("PASS: " + testName);
					testsPassed++;
			} else {
					System.out.println("FAIL: " + testName + " | Expected: " + expected + " | Got: " + result);
					testsFailed++;
			}
	}

	// Helper to check that a RuntimeException is thrown with the correct message
	static void assertError(String testName, Runnable test, String expectedMessage) {
			try {
					test.run();
					System.out.println("FAIL: " + testName + " | Expected exception but none was thrown");
					testsFailed++;
			} catch (RuntimeException e) {
					if (e.getMessage().equals(expectedMessage)) {
							System.out.println("PASS: " + testName);
							testsPassed++;
					} else {
							System.out.println("FAIL: " + testName + " | Expected: " + expectedMessage + " | Got: " + e.getMessage());
							testsFailed++;
					}
			}
	}

	public static void main(String[] args) {

			// -------------------------------------------------------
			// LITERAL TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Literal Tests ---");
			Interpreter literalInterp = new Interpreter();

			assertEqual("Integer literal",
							literalInterp.visitLiteralExpr(new Expr.Literal(42)),
							42);

			assertEqual("Boolean literal true",
							literalInterp.visitLiteralExpr(new Expr.Literal(true)),
							true);

			assertEqual("Boolean literal false",
				literalInterp.visitLiteralExpr(new Expr.Literal(false)),
				false);

			// -------------------------------------------------------
			// UNARY TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Unary Tests ---");
			Interpreter unaryInterp = new Interpreter();

			assertEqual("Unary minus",
							unaryInterp.visitUnaryExpr(new Expr.Unary(Operator.MINUS, new Expr.Literal(5))),
							-5);

			assertEqual("Unary not true",
							unaryInterp.visitUnaryExpr(new Expr.Unary(Operator.NOT, new Expr.Literal(true))),
							false);

			assertEqual("Unary not false",
							unaryInterp.visitUnaryExpr(new Expr.Unary(Operator.NOT, new Expr.Literal(false))),
							true);

			assertError("Unary minus on bool",
							() -> unaryInterp.visitUnaryExpr(new Expr.Unary(Operator.MINUS, new Expr.Literal(true))),
							"Error: type mismatch");

			assertError("Unary not on int",
							() -> unaryInterp.visitUnaryExpr(new Expr.Unary(Operator.NOT, new Expr.Literal(5))),
							"Error: type mismatch");

			// -------------------------------------------------------
			// BINARY ARITHMETIC TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Binary Arithmetic Tests ---");
			Interpreter arithmeticInterp = new Interpreter();

			assertEqual("Addition",
							arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(3), Operator.PLUS, new Expr.Literal(4))),
							7);

			assertEqual("Subtraction",
							arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(10), Operator.MINUS, new Expr.Literal(3))),
							7);

			assertEqual("Multiplication",
							arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(3), Operator.MULTIPLY, new Expr.Literal(4))),
							12);

			assertEqual("Division",
							arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(10), Operator.DIVIDE, new Expr.Literal(2))),
							5);

			assertEqual("Modulo",
							arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(10), Operator.MODULO, new Expr.Literal(3))),
							1);

			assertError("Division by zero",
							() -> arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(10), Operator.DIVIDE, new Expr.Literal(0))),
							"Error: arithmetic error");

			assertError("Modulo by zero",
							() -> arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(10), Operator.MODULO, new Expr.Literal(0))),
							"Error: arithmetic error");

			assertError("Addition type mismatch",
							() -> arithmeticInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(1), Operator.PLUS, new Expr.Literal(true))),
							"Error: type mismatch");

			// -------------------------------------------------------
			// BINARY COMPARISON TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Binary Comparison Tests ---");
			Interpreter comparisonInterp = new Interpreter();

			assertEqual("Less than true",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(3), Operator.LESS, new Expr.Literal(5))),
							true);

			assertEqual("Less than false",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.LESS, new Expr.Literal(3))),
							false);

			assertEqual("Less equal",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.LESS_EQUAL, new Expr.Literal(5))),
							true);

			assertEqual("Greater than",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.GREATER, new Expr.Literal(3))),
							true);

			assertEqual("Greater equal",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.GREATER_EQUAL, new Expr.Literal(5))),
							true);

			assertEqual("Equal integers true",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.EQUAL, new Expr.Literal(5))),
							true);

			assertEqual("Equal integers false",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.EQUAL, new Expr.Literal(3))),
							false);

			assertEqual("Equal booleans",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(true), Operator.EQUAL, new Expr.Literal(true))),
							true);

			assertEqual("Not equal integers",
							comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(5), Operator.NOT_EQUAL, new Expr.Literal(3))),
							true);

			assertError("Equal type mismatch int and bool",
							() -> comparisonInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(1), Operator.EQUAL, new Expr.Literal(true))),
							"Error: type mismatch");

			// -------------------------------------------------------
			// LOGICAL OPERATOR TESTS (including short circuit)
			// -------------------------------------------------------
			System.out.println("\n--- Logical Operator Tests ---");
			Interpreter logicalInterp = new Interpreter();

			assertEqual("AND true true",
							logicalInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(true), Operator.AND, new Expr.Literal(true))),
							true);

			assertEqual("AND true false",
							logicalInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(true), Operator.AND, new Expr.Literal(false))),
							false);

			assertEqual("AND false short circuits",
							logicalInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(false), Operator.AND,
											new Expr.Binary(new Expr.Literal(1), Operator.DIVIDE, new Expr.Literal(0)))),
							false); // rhs should never be evaluated

			assertEqual("OR true short circuits",
							logicalInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(true), Operator.OR,
											new Expr.Binary(new Expr.Literal(1), Operator.DIVIDE, new Expr.Literal(0)))),
							true); // rhs should never be evaluated

			assertEqual("OR false false",
							logicalInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(false), Operator.OR, new Expr.Literal(false))),
							false);

			assertError("AND with int operand",
							() -> logicalInterp.visitBinaryExpr(new Expr.Binary(new Expr.Literal(1), Operator.AND, new Expr.Literal(true))),
							"Error: type mismatch");

			// -------------------------------------------------------
			// VARIABLE DECLARATION AND LOOKUP TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Variable Tests ---");
			Interpreter varDeclInterp = new Interpreter();

			varDeclInterp.visitVarStmt(new Stmt.Var("x", VarType.INT, new Expr.Literal(42)));
			assertEqual("Variable lookup after declaration",
							varDeclInterp.visitVariableExpr(new Expr.Variable("x")),
							42);

			varDeclInterp.visitVarStmt(new Stmt.Var("flag", VarType.BOOL, new Expr.Literal(true)));
			assertEqual("Boolean variable lookup",
							varDeclInterp.visitVariableExpr(new Expr.Variable("flag")),
							true);

			assertError("Unbound variable",
							() -> varDeclInterp.visitVariableExpr(new Expr.Variable("undefined")),
							"Error: unbound reference");

			// -------------------------------------------------------
			// ASSIGNMENT TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Assignment Tests ---");
			Interpreter assignInterp = new Interpreter();

			assignInterp.visitVarStmt(new Stmt.Var("x", VarType.INT, new Expr.Literal(10)));
			assignInterp.visitAssignStmt(new Stmt.Assign("x", new Expr.Literal(20)));
			assertEqual("Variable reassignment",
							assignInterp.visitVariableExpr(new Expr.Variable("x")),
							20);

			assertError("Assign to undeclared variable",
							() -> assignInterp.visitAssignStmt(new Stmt.Assign("y", new Expr.Literal(5))),
							"Error: unbound reference");

			// -------------------------------------------------------
			// SCOPING AND SHADOWING TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Scoping Tests ---");
			Interpreter scopeInterp = new Interpreter();

			// x = 10 in outer scope, x = 20 in inner scope, x should be 10 after block
			scopeInterp.visitVarStmt(new Stmt.Var("x", VarType.INT, new Expr.Literal(10)));
			scopeInterp.visitBlockStmt(new Stmt.Block(Arrays.asList(
							new Stmt.Var("x", VarType.INT, new Expr.Literal(20))
			)));
			assertEqual("Outer variable unchanged after inner shadow",
							scopeInterp.visitVariableExpr(new Expr.Variable("x")),
							10);

			// Assign to outer x from inner block
			scopeInterp.visitVarStmt(new Stmt.Var("y", VarType.INT, new Expr.Literal(10)));
			scopeInterp.visitBlockStmt(new Stmt.Block(Arrays.asList(
							new Stmt.Assign("y", new Expr.Literal(99))
			)));
			assertEqual("Outer variable updated by inner assignment",
							scopeInterp.visitVariableExpr(new Expr.Variable("y")),
							99);

			// -------------------------------------------------------
			// IF STATEMENT TESTS
			// -------------------------------------------------------
			System.out.println("\n--- If Statement Tests ---");
			Interpreter ifStmtInterp = new Interpreter();

			ifStmtInterp.visitVarStmt(new Stmt.Var("result", VarType.INT, new Expr.Literal(0)));
			ifStmtInterp.visitIfStmt(new Stmt.If(
							new Expr.Literal(true),
							new Stmt.Assign("result", new Expr.Literal(1)),
							new Stmt.Assign("result", new Expr.Literal(2))
			));
			assertEqual("If true branch taken",
							ifStmtInterp.visitVariableExpr(new Expr.Variable("result")),
							1);

			ifStmtInterp.visitIfStmt(new Stmt.If(
							new Expr.Literal(false),
							new Stmt.Assign("result", new Expr.Literal(1)),
							new Stmt.Assign("result", new Expr.Literal(2))
			));
			assertEqual("If false branch taken",
							ifStmtInterp.visitVariableExpr(new Expr.Variable("result")),
							2);

			ifStmtInterp.visitIfStmt(new Stmt.If(
							new Expr.Literal(false),
							new Stmt.Assign("result", new Expr.Literal(99)),
							null
			));
			assertEqual("If false no else branch",
							ifStmtInterp.visitVariableExpr(new Expr.Variable("result")),
							2); // unchanged

			assertError("If non-boolean condition",
							() -> ifStmtInterp.visitIfStmt(new Stmt.If(
											new Expr.Literal(1),
											new Stmt.Assign("result", new Expr.Literal(1)),
											null
							)),
							"Error: type mismatch");

			// -------------------------------------------------------
			// WHILE LOOP TESTS
			// -------------------------------------------------------
			System.out.println("\n--- While Loop Tests ---");
			Interpreter whileStmtInterp = new Interpreter();

			// x starts at 5, decrement until 0, result should be 0
			whileStmtInterp.visitVarStmt(new Stmt.Var("x", VarType.INT, new Expr.Literal(5)));
			whileStmtInterp.visitWhileStmt(new Stmt.While(
							new Expr.Binary(new Expr.Variable("x"), Operator.GREATER, new Expr.Literal(0)),
							new Stmt.Assign("x", new Expr.Binary(new Expr.Variable("x"), Operator.MINUS, new Expr.Literal(1)))
			));
			assertEqual("While loop decrements to 0",
							whileStmtInterp.visitVariableExpr(new Expr.Variable("x")),
							0);

			assertError("While non-boolean condition",
							() -> whileStmtInterp.visitWhileStmt(new Stmt.While(
											new Expr.Literal(1),
											new Stmt.Assign("x", new Expr.Literal(0))
							)),
							"Error: type mismatch");

			// -------------------------------------------------------
			// FUNCTION DEFINITION AND CALL TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Function Tests ---");
			Interpreter funcDeclInterp = new Interpreter();

			// fn add(x: int, y: int): int { return x + y; }
			funcDeclInterp.visitFunctionStmt(new Stmt.Function(
							"add",
							VarType.INT,
							Arrays.asList(
											new Stmt.Parameter("x", VarType.INT),
											new Stmt.Parameter("y", VarType.INT)
							),
							Arrays.asList(
											new Stmt.Return(
															new Expr.Binary(new Expr.Variable("x"), Operator.PLUS, new Expr.Variable("y"))
											)
							)
			));
			assertEqual("Simple function call",
							funcDeclInterp.visitCallExpr(new Expr.Call("add", Arrays.asList(new Expr.Literal(3), new Expr.Literal(4)))),
							7);

			assertError("Call undeclared function",
							() -> funcDeclInterp.visitCallExpr(new Expr.Call("undefined", Arrays.asList())),
							"Error: unbound reference");

			assertError("Wrong number of arguments",
							() -> funcDeclInterp.visitCallExpr(new Expr.Call("add", Arrays.asList(new Expr.Literal(1)))),
							"Error: type mismatch");

			// -------------------------------------------------------
			// RECURSIVE FUNCTION TEST (factorial)
			// -------------------------------------------------------
			System.out.println("\n--- Recursion Tests ---");
			Interpreter recursiveInterp = new Interpreter();

			// fn factorial(n: int): int { if (n <= 1) { return 1; } else { return n * factorial(n-1); } }
			recursiveInterp.visitFunctionStmt(new Stmt.Function(
							"factorial",
							VarType.INT,
							Arrays.asList(new Stmt.Parameter("n", VarType.INT)),
							Arrays.asList(
											new Stmt.If(
															new Expr.Binary(new Expr.Variable("n"), Operator.LESS_EQUAL, new Expr.Literal(1)),
															new Stmt.Return(new Expr.Literal(1)),
															new Stmt.Return(
																			new Expr.Binary(
																							new Expr.Variable("n"),
																							Operator.MULTIPLY,
																							new Expr.Call("factorial", Arrays.asList(
																											new Expr.Binary(new Expr.Variable("n"), Operator.MINUS, new Expr.Literal(1))
																							))
																			)
															)
											)
							)
			));

			assertEqual("Factorial of 5",
							recursiveInterp.visitCallExpr(new Expr.Call("factorial", Arrays.asList(new Expr.Literal(5)))),
							120);

			assertEqual("Factorial of 1",
							recursiveInterp.visitCallExpr(new Expr.Call("factorial", Arrays.asList(new Expr.Literal(1)))),
							1);

			assertEqual("Factorial of 0",
							recursiveInterp.visitCallExpr(new Expr.Call("factorial", Arrays.asList(new Expr.Literal(0)))),
							1);

			// -------------------------------------------------------
			// NESTED FUNCTION TESTS
			// -------------------------------------------------------
			System.out.println("\n--- Nested Function Tests ---");
			Interpreter nestedInterp = new Interpreter();

			// fn outer(): int { x: int = 10; fn inner(): int { return x; } return inner(); }
			nestedInterp.visitFunctionStmt(new Stmt.Function(
							"outer",
							VarType.INT,
							Arrays.asList(),
							Arrays.asList(
											new Stmt.Var("x", VarType.INT, new Expr.Literal(10)),
											new Stmt.Function(
															"inner",
															VarType.INT,
															Arrays.asList(),
															Arrays.asList(new Stmt.Return(new Expr.Variable("x")))
											),
											new Stmt.Return(new Expr.Call("inner", Arrays.asList()))
							)
			));

			assertEqual("Nested function captures outer variable",
							nestedInterp.visitCallExpr(new Expr.Call("outer", Arrays.asList())),
							10);


			// -------------------------------------------------------
			// TEST 1: Basic output from main
			// fn main(): int { output(42); return 0; }
			// Expected output: 42
			// -------------------------------------------------------
			System.out.println("--- Test 1: Basic output from main ---");
			Interpreter interp1 = new Interpreter();
			interp1.interpretProgram(Arrays.asList(
					new Stmt.Function(
							"main",
							VarType.INT,
							Arrays.asList(),
							Arrays.asList(
									new Stmt.Expression(
											new Expr.Call("output", Arrays.asList(new Expr.Literal(42)))
									),
									new Stmt.Return(new Expr.Literal(0))
							)
					)
			));

			// -------------------------------------------------------
			// TEST 2: Global variable used in main
			// x: int = 10; fn main(): int { output(x); return 0; }
			// Expected output: 10
			// -------------------------------------------------------
			System.out.println("--- Test 2: Global variable used in main ---");
			Interpreter interp2 = new Interpreter();
			interp2.interpretProgram(Arrays.asList(
					new Stmt.Var("x", VarType.INT, new Expr.Literal(10)),
					new Stmt.Function(
							"main",
							VarType.INT,
							Arrays.asList(),
							Arrays.asList(
									new Stmt.Expression(
											new Expr.Call("output", Arrays.asList(new Expr.Variable("x")))
									),
									new Stmt.Return(new Expr.Literal(0))
							)
					)
			));

			// -------------------------------------------------------
			// TEST 3: Factorial called from main
			// fn factorial(n: int): int { ... }
			// fn main(): int { output(factorial(5)); return 0; }
			// Expected output: 120
			// -------------------------------------------------------
			System.out.println("--- Test 3: Factorial from main ---");
			Interpreter interp3 = new Interpreter();
			interp3.interpretProgram(Arrays.asList(
					new Stmt.Function(
							"factorial",
							VarType.INT,
							Arrays.asList(new Stmt.Parameter("n", VarType.INT)),
							Arrays.asList(
									new Stmt.If(
											new Expr.Binary(new Expr.Variable("n"), Operator.LESS_EQUAL, new Expr.Literal(1)),
											new Stmt.Return(new Expr.Literal(1)),
											new Stmt.Return(
													new Expr.Binary(
															new Expr.Variable("n"),
															Operator.MULTIPLY,
															new Expr.Call("factorial", Arrays.asList(
																	new Expr.Binary(new Expr.Variable("n"), Operator.MINUS, new Expr.Literal(1))
															))
													)
											)
									)
							)
					),
					new Stmt.Function(
							"main",
							VarType.INT,
							Arrays.asList(),
							Arrays.asList(
									new Stmt.Expression(
											new Expr.Call("output", Arrays.asList(
													new Expr.Call("factorial", Arrays.asList(new Expr.Literal(5)))
											))
									),
									new Stmt.Return(new Expr.Literal(0))
							)
					)
			));

			// -------------------------------------------------------
			// TEST 4: While loop counting down
			// fn main(): int { x: int = 5; while (x > 0) { output(x); x = x - 1; } return 0; }
			// Expected output: 5 4 3 2 1
			// -------------------------------------------------------
			System.out.println("--- Test 4: While loop countdown ---");
			Interpreter interp4 = new Interpreter();
			interp4.interpretProgram(Arrays.asList(
					new Stmt.Function(
							"main",
							VarType.INT,
							Arrays.asList(),
							Arrays.asList(
									new Stmt.Var("x", VarType.INT, new Expr.Literal(5)),
									new Stmt.While(
											new Expr.Binary(new Expr.Variable("x"), Operator.GREATER, new Expr.Literal(0)),
											new Stmt.Block(Arrays.asList(
													new Stmt.Expression(
															new Expr.Call("output", Arrays.asList(new Expr.Variable("x")))
													),
													new Stmt.Assign("x",
															new Expr.Binary(new Expr.Variable("x"), Operator.MINUS, new Expr.Literal(1))
													)
											))
									),
									new Stmt.Return(new Expr.Literal(0))
							)
					)
			));

			// -------------------------------------------------------
			// TEST 5: Runtime error handled gracefully - division by zero
			// fn main(): int { x: int = 1 / 0; return 0; }
			// Expected: prints "Error: arithmetic error" and exits
			// -------------------------------------------------------
			System.out.println("--- Test 5: Division by zero handled gracefully ---");
			Interpreter interp5 = new Interpreter();
			interp5.interpretProgram(Arrays.asList(
					new Stmt.Function(
							"main",
							VarType.INT,
							Arrays.asList(),
							Arrays.asList(
									new Stmt.Var("x", VarType.INT,
											new Expr.Binary(new Expr.Literal(1), Operator.DIVIDE, new Expr.Literal(0))
									),
									new Stmt.Return(new Expr.Literal(0))
							)
					)
			));

			// -------------------------------------------------------
			// SUMMARY
			// -------------------------------------------------------
			System.out.println("\n--- Test Summary ---");
			System.out.println("Passed: " + testsPassed);
			System.out.println("Failed: " + testsFailed);
			System.out.println("Total:  " + (testsPassed + testsFailed));
    }
}
