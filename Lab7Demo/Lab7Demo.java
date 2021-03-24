import java.util.Stack;

interface RendererBridge
{

    void renderCircle(float radiusX, float radiusY);
    void renderRect(float sideX, float sideY);

}

class VectorRendererBridge implements RendererBridge
{

   @Override
   public void renderCircle(float radiusX, float radiusY)
   {
       System.out.println("Drawing a *vector* circle of radius X "+radiusX+" and radius Y "+radiusY);
   }

   @Override
   public void renderRect(float sideX, float sideY)
   {
       System.out.println("Drawing a *vector* rectangle of side X "+sideX+" and side Y "+sideY);
   }

}

class RasterRendererBridge implements RendererBridge
{

    @Override
    public void renderCircle(float radiusX, float radiusY)
    {
        System.out.println("Drawing a *raster* circle of radius X "+radiusX+" and radius Y "+radiusY);
    }
 
    @Override
    public void renderRect(float sideX, float sideY)
    {
        System.out.println("Drawing a *raster* rectangle of side X "+sideX+" and side Y "+sideY);
    }

}

abstract class Shape
{

    protected RendererBridge renderer;

    public Shape(RendererBridge renderer)
    {
        this.renderer = renderer;
    }
    public abstract void draw();
    public abstract void resize(float scaleX, float scaleY);

}

class Circle extends Shape implements Prototype
{

    public float radiusX, radiusY;

    public Circle(RendererBridge renderer)
    {
        super(renderer);
    }

    public Circle(RendererBridge renderer, float radius)
    {
        super(renderer);
        this.radiusX = radius;
        this.radiusY = radius;
    }

    @Override
    public void draw()
    {
        renderer.renderCircle(radiusX, radiusY);
    }

    @Override
    public void resize(float scaleX, float scaleY)
    {
        radiusX *= scaleX;
        radiusY *= scaleY;
    }

    public CircleMemento resizeMemento(float scaleX, float scaleY)
    {
        CircleMemento old = new CircleMemento(radiusX, radiusY);
        resize(scaleX, scaleY);
        return old;
    }

    public void restoreMemento(CircleMemento c)
    {
        this.radiusX = c.radiusX;
        this.radiusY = c.radiusY;
    }

    @Override
    public Prototype clonePrototype()
    {
        Circle newCircle = new Circle(renderer);
        newCircle.radiusX = radiusX;
        newCircle.radiusY = radiusY;
        return newCircle;
    }

}

class Rectangle extends Shape
{

    public float sideX, sideY;

    public Rectangle(RendererBridge renderer)
    {
        super(renderer);
    }

    public Rectangle(RendererBridge renderer, float sideX, float sideY)
    {
        super(renderer);
        this.sideX = sideX;
        this.sideY = sideY;
    }

    @Override
    public void draw()
    {
        renderer.renderRect(sideX, sideY);
    }

    @Override
    public void resize(float scaleX, float scaleY)
    {
        scaleX *= scaleX;
        scaleY *= scaleY;
    }

}

//

interface Command
{
    void call();
    void undo();
}

class ResizeShapeCommand implements Command
{

    private Shape shape;

    public enum Action
    {
        WIDTHRESIZING, HEIGHIRESIZING, BOTHRESIZING
    }

    private Action action;
    private float scale;

    public ResizeShapeCommand(Shape shape, Action action, float scale)
    {
        this.shape = shape;
        this.action = action;
        this.scale = scale;
    }

    @Override
    public void call()
    {
        switch(action)
        {
            case WIDTHRESIZING: shape.resize(scale, 1); break;
            case HEIGHIRESIZING: shape.resize(1, scale);    break;
            case BOTHRESIZING: shape.resize(scale, scale);  break;
        }
    }

    @Override
    public void undo()
    {
        switch(action)
        {
            case WIDTHRESIZING: shape.resize(1/scale, 1); break;
            case HEIGHIRESIZING: shape.resize(1, 1/scale);    break;
            case BOTHRESIZING: shape.resize(1/scale, 1/scale);  break;
        }
    }

}

//
class CircleMemento
{
    public float radiusX, radiusY;

    public CircleMemento(float radiusX, float radiusY)
    {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }
}

//
interface Prototype
{
    Prototype clonePrototype();
}

//
class MementoSingleton
{
    private static MementoSingleton INSTANCE;
    private Stack<CircleMemento> mementos;

    protected MementoSingleton()
    {
        System.out.println("Singleton is initialising");
        mementos = new Stack<>();
    }

    public static MementoSingleton getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new MementoSingleton();
        }
        return INSTANCE; 
    }

    public void setMemento(CircleMemento c)
    {
        mementos.push(c);
    }

    public CircleMemento getLastMemento()
    {
        return mementos.pop();
    }
}

class Lab7Demo
{

    public static void main(String[] args)
    {
        System.out.println("==BRIDGE==");
        RasterRendererBridge raster = new RasterRendererBridge();
        VectorRendererBridge vector = new VectorRendererBridge();

        Circle circle = new Circle(vector, 5);
        circle.draw();
        circle.resize(2, 2);
        circle.draw();

        Rectangle rect = new Rectangle(raster, 10, 5);
        rect.draw();

        //

        System.out.println("==COMMAND==");

        ResizeShapeCommand resizeShapeCommand = new ResizeShapeCommand(circle, ResizeShapeCommand.Action.WIDTHRESIZING, 4);
        resizeShapeCommand.call();
        circle.draw();

        Stack<Command> commands = new Stack<>();

        commands.push(new ResizeShapeCommand(circle, ResizeShapeCommand.Action.WIDTHRESIZING, 10));
        commands.push(new ResizeShapeCommand(circle, ResizeShapeCommand.Action.HEIGHIRESIZING, 20));
        commands.push(new ResizeShapeCommand(circle, ResizeShapeCommand.Action.BOTHRESIZING, 40));

        for(Command c : commands)
        {
            c.call();
            circle.draw();
        }

        System.out.println("==UNDO COMMAND==");

        while(!commands.isEmpty())
        {
            Command c = commands.pop();
            c.undo();
            circle.draw();
        }

        //

        System.out.println("==MEMENTO==");
        CircleMemento c1 = circle.resizeMemento(10,20);
        CircleMemento c2 = circle.resizeMemento(20,20);
        Stack<CircleMemento> mementos = new Stack<>();
        mementos.push(circle.resizeMemento(10, 10));
        mementos.push(circle.resizeMemento(20, 20));
        mementos.push(circle.resizeMemento(30, 30));

        circle.restoreMemento(mementos.pop());
        circle.draw();

        circle.restoreMemento(mementos.pop());
        circle.draw();

        //

        System.out.println("==PROTOTYPE==");
        mementos.push(circle.resizeMemento(30, 30));

        Circle newCircle1 = (Circle)circle.clonePrototype();
        newCircle1.draw();

        circle.restoreMemento(mementos.pop());

        Circle newCircle2 = (Circle)circle.clonePrototype();
        newCircle2.draw();

        //

        System.out.println("==SINGLETON==");
        MementoSingleton.getInstance().setMemento(circle.resizeMemento(10, 20));
        circle.draw();
        MementoSingleton.getInstance().setMemento(circle.resizeMemento(20, 20));
        circle.draw();

        circle.restoreMemento(MementoSingleton.getInstance().getLastMemento());
        circle.draw();

        circle.restoreMemento(MementoSingleton.getInstance().getLastMemento());
        circle.draw();
    }

}