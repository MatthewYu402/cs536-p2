package madlang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    Environment globalEnv;
    Environment currentEnv;
    Scanner scanner = new Scanner(System.in);

    // Interface for user-defined and built in functions
    interface MadlangCallable {
      Object call(List<Object> args);
    }

    // Helper function to use visitor pattern on the body
    void executeBlock(Stmt.Block block, Environment env) {
      Environment previous = currentEnv;

      try {
          currentEnv = env;
          for (Stmt stmt : block.statements) {
              stmt.accept(this);
          }
      } finally {
        currentEnv = previous;
      }
    }

    // Special exception to carry return value from nested visitor call
    class ReturnException extends RuntimeException {
      Object value;
      ReturnException(Object value) {
        this.value = value;
      }
    }

    // Nested class for user-defined functions
    class MadlangFunction implements MadlangCallable {
      Stmt.Function stmt;
      Environment closure; // environment at time the function was called (parent or global)

      public MadlangFunction(Stmt.Function stmt, Environment closure) {
        this.stmt = stmt;
        this.closure = closure;
      }

      @Override
      public Object call(List<Object> args) {
        // Check for correct number of arguments
        if (args.size() != stmt.params.size()) {
            throw new RuntimeException("Error: type mismatch");
        }

        // Map value from function call to parameters
        Environment localEnvironment = new Environment(closure);
        for (int i = 0; i < args.size(); i++) {
          localEnvironment.declare(stmt.params.get(i).name(), args.get(i));
        }

        try {
          executeBlock(new Stmt.Block(stmt.body), localEnvironment); 
        } catch (ReturnException e) {
            return e.value;
        }
        return null;

      }
    }

    // Starting point of the program
    void interpretProgram(List<Stmt> stmts) {
      try {
        for (Stmt stmt : stmts) {
            stmt.accept(this);
        }
        Object mainProgram = globalEnv.get("main");

        if (!(mainProgram instanceof MadlangCallable)) {
          throw new RuntimeException("Error: unbound reference");
        }

        ((MadlangCallable) mainProgram).call(new ArrayList<>()); // Call it
      } catch (RuntimeException e) {
        if (e instanceof ReturnException) {
          System.err.println("Error: unexpected return");
        } else {
            System.err.println(e.getMessage());
        }
        System.exit(1);
      }
    }

    Interpreter() {
      this.globalEnv = new Environment(null);
      this.currentEnv = globalEnv;
      // Handle built-in output
      globalEnv.declare("output", new MadlangCallable() {
        @Override
        public Object call(List<Object> args) {
          if (args.size() != 1) {
            throw new RuntimeException("Error: type mismatch");
          }
          Object argument = args.get(0);
          if (!(argument instanceof Integer)) {
            throw new RuntimeException("Error: type mismatch");
          }
          System.out.println(argument);
          return null; // Doesn't matter
        }
      });

      // Handle built-in input
      globalEnv.declare("input", new MadlangCallable() {
        @Override
        public Object call(List<Object> args) {
          if (!args.isEmpty()) {
            throw new RuntimeException("Error: type mismatch");
          }

          try {
            String argument;
            argument = scanner.nextLine();
            return Integer.valueOf(argument);
          } catch (Exception e) {
            throw new RuntimeException("Error: type mismatch");
          }
        }
      });

    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        currentEnv.declare(stmt.name, new MadlangFunction(stmt, currentEnv));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
      Object condition = stmt.condition.accept(this);
      if (!(condition instanceof Boolean)) {
        throw new RuntimeException("Error: type mismatch");
      }

      if ((Boolean) condition) {
        stmt.thenBranch.accept(this);
      } else if (stmt.elseBranch != null) {
        stmt.elseBranch.accept(this);
      }
      return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
      throw new ReturnException(stmt.value.accept(this));
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
      executeBlock(stmt, new Environment(currentEnv));
      return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
      if (stmt.initializer != null) {
        Object expression = stmt.initializer.accept(this);
        currentEnv.declare(stmt.name, expression);
      } else {
        currentEnv.declare(stmt.name, null);
      }
      return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
      while (true) { 
        Object condition = stmt.condition.accept(this);
        if (!(condition instanceof Boolean)) {
          throw new RuntimeException("Error: type mismatch");
        }
        if (!(Boolean) condition) break;
        stmt.body.accept(this);
      }
      return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
      stmt.expression.accept(this);
      return null;
    }

    @Override
    public Void visitAssignStmt(Stmt.Assign stmt) {
        Object rhs = stmt.value.accept(this);
        currentEnv.assign(stmt.name, rhs);
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // Evaluate left and right operands, then apply the operator
        // Handle arithmetic (+, -, *, /), comparison (==, !=, <, <=, >, >=), and logical (&&, ||) operators
        Object lhs = expr.left.accept(this);

        // AND Case
        if (expr.operator == Operator.AND) {
          if (!(lhs instanceof Boolean)) {
            throw new RuntimeException("Error: type mismatch");
          }

          if (!(Boolean) lhs) {
            return false;
          }

          Object rhs = expr.right.accept(this);

          if (!(rhs instanceof Boolean)) {
            throw new RuntimeException("Error: type mismatch");
          }
          return (Boolean) rhs;
        }

        // OR Case
        if (expr.operator == Operator.OR) {
          if (!(lhs instanceof Boolean)) {
            throw new RuntimeException("Error: type mismatch");
          }

          if ((Boolean) lhs) {
            return true;
          }

          Object rhs = expr.right.accept(this);

          if (!(rhs instanceof Boolean)) {
            throw new RuntimeException("Error: type mismatch");
          }
          return (Boolean) rhs;
        }


        Object rhs = expr.right.accept(this);
        switch (expr.operator) {
          // Arithmetic Operatiors
          case PLUS:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            } 
            return (Integer) lhs + (Integer) rhs;
              
          case MINUS:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return (Integer) lhs - (Integer) rhs;

          case MULTIPLY:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return (Integer) lhs * (Integer) rhs;

          case DIVIDE:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            // Divide by zero
            if ((Integer) rhs == 0) {
              throw new RuntimeException("Error: arithmetic error");
            }
            return (Integer) lhs / (Integer) rhs;

          case MODULO:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
                throw new RuntimeException("Error: type mismatch");
            }
            // Mod by zero
            if ((Integer) rhs == 0) {
              throw new RuntimeException("Error: arithmetic error");
            }
            return (Integer) lhs % (Integer) rhs;

          case EQUAL:
            if (!(lhs instanceof Integer && rhs instanceof Integer) && 
            !(lhs instanceof Boolean && rhs instanceof Boolean)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return Objects.equals(lhs, rhs);

          case NOT_EQUAL:
            if (!(lhs instanceof Integer && rhs instanceof Integer) && 
            !(lhs instanceof Boolean && rhs instanceof Boolean)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return !Objects.equals(lhs, rhs);
          
          case LESS:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return (Integer) lhs < (Integer) rhs;

          case LESS_EQUAL:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return (Integer) lhs <= (Integer) rhs;

          case GREATER:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return (Integer) lhs > (Integer) rhs;

          case GREATER_EQUAL:
            if (!(lhs instanceof Integer && rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
            }
            return (Integer) lhs >= (Integer) rhs;

          default:
            throw new RuntimeException("Error: type mismatch");
        }
        
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
      // Evaluate the right operand and apply the unary operator (!, -)
      Object rhs = expr.right.accept(this);

      switch (expr.operator) {
        case MINUS:
          if (!(rhs instanceof Integer)) {
              throw new RuntimeException("Error: type mismatch");
          }
          return -(Integer) rhs;

        case NOT:
          if (!(rhs instanceof Boolean)) {
            throw new RuntimeException("Error: type mismatch");
          }
          return !(Boolean) rhs;

        default:
          throw new RuntimeException("Error: type mismatch");
      }

    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
      return currentEnv.get(expr.name);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
      Object funcName = currentEnv.get(expr.name);
      
      if (!(funcName instanceof MadlangCallable)) {
        throw new RuntimeException("Error: unbound reference");
      } 

      List<Object> arguments = new ArrayList<>();
      
      for (Expr arg : expr.arguments) {
        arguments.add(arg.accept(this));
      }

      return ((MadlangCallable) funcName).call(arguments);
  
    }

}
