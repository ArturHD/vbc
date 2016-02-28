package edu.cmu.cs.vbc

import java.io.{File, FileOutputStream, InputStream}

import edu.cmu.cs.vbc.loader.Loader
import edu.cmu.cs.vbc.vbytecode.VBCClassNode
import org.objectweb.asm.util.{CheckClassAdapter, TraceClassVisitor}
import org.objectweb.asm.{ClassReader, ClassVisitor, ClassWriter}

/**
  * Custom class loader to modify bytecode before loading the class.
  */
class VBCClassLoader(isLift: Boolean = false) extends ClassLoader {

    val loader = new Loader()

    override def loadClass(name: String): Class[_] = {
        if (filterByName(name)) super.loadClass(name) else findClass(name)
    }

    override def findClass(name: String): Class[_] = {
        val resource: String = name.replace('.', '/') + ".class"
        val is: InputStream = getResourceAsStream(resource)
        val clazz: VBCClassNode = loader.loadClass(is)


        val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) // COMPUTE_FRAMES implies COMPUTE_MAX
        if (isLift)
            clazz.toVByteCode(cw)
        else
            clazz.toByteCode(cw)

        val cr2 = new ClassReader(cw.toByteArray)
        cr2.accept(getCheckClassAdapter(getTraceClassVisitor(null)), 0)
        // for debugging
        //    toFile(name, cw)
        defineClass(name, cw.toByteArray, 0, cw.toByteArray.length)
    }

    /**
      * Get the default TraceClassVisitor chain, which simply prints the bytecode
      *
      * @param next next ClassVisitor in the chain, usually a ClassWriter in this case
      * @return a ClassVisitor that should be accepted by ClassReader
      */
    def getTraceClassVisitor(next: ClassVisitor): ClassVisitor = new TraceClassVisitor(next, null)

    def getCheckClassAdapter(next: ClassVisitor): ClassVisitor = new CheckClassAdapter(next)

    /**
      * Filter classes to modify by their names
      *
      * @param name (partial) name of the class that SHOULD be modified
      * @return false if the class needs to be modified
      */
    private def filterByName(name: String): Boolean = !name.startsWith("edu.cmu.cs.vbc.prog")


    def toFile(name: String, cw: ClassWriter) = {
        val replaced = name.replace(".", "/")
        println(replaced)
        val file = new File("lifted/" + replaced)
        file.getParentFile.mkdirs()
        val outFile = new FileOutputStream("lifted/" + replaced + ".class")
        outFile.write(cw.toByteArray)
    }
}
