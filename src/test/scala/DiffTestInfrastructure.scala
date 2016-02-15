package edu.cmu.cs.vbc

import java.io.PrintWriter

import de.fosd.typechef.conditional.{ConditionalLib, Opt}
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.instructions.{MyMethodNode, Rewrite}
import edu.cmu.cs.vbc.test.TestOutput.TOpt
import edu.cmu.cs.vbc.test.{Config, InstrLoadConfig, TestOutput}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.{ClassReader, ClassWriter}

/**
  * compares the execution of two classes
  */
trait DiffTestInfrastructure {

    class MyClassLoader extends ClassLoader(this.getClass.getClassLoader) {
        def defineClass(name: String, b: Array[Byte]): Class[_] = {
            return defineClass(name, b, 0, b.length)
        }
    }



    case class TestClass(m: MyMethodNode) {
        private def header(cw: ClassWriter): Unit = {
            cw.newClass("Test")
            cw.visit(V1_8, ACC_PUBLIC, "Test", null, "java/lang/Object", Array.empty)

            //default constructor:
            val mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", "()V", Array.empty)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            mv.visitInsn(RETURN)
            mv.visitMaxs(0, 0)
            mv.visitEnd()
        }

        def toByteCode(cw: ClassWriter) = {
            header(cw)
            m.toByteCode(cw)
            cw.visitEnd()
        }

        def toVByteCode(cw: ClassWriter) = {
            header(cw)
            Rewrite.rewrite(m).toVByteCode(cw)
            cw.visitEnd()
        }

    }


    def loadTestClass(clazz: TestClass): Class[_] = {
        val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        clazz.toByteCode(cw)
        val byte = cw.toByteArray
        val myClassLoader = new MyClassLoader
        myClassLoader.defineClass("Test", byte)
    }

    def loadVTestClass(clazz: TestClass): Class[_] = {
        val vcw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        clazz.toVByteCode(vcw)
        val vbyte = vcw.toByteArray

        val printer = new TraceClassVisitor(new PrintWriter(System.out))
        new ClassReader(vbyte).accept(printer, 0)
        val myVClassLoader = new MyClassLoader
        myVClassLoader.defineClass("Test", vbyte)
    }


    def testMethod(m: MyMethodNode): Unit = {

        val clazz = new TestClass(m)


        val configOptions: Set[String] = getConfigOptions(m)


        //        val resource: String = "edu.cmu.cs.vbc.Tmp".replace('.', '/') + ".class"
        //        val is: InputStream = myClassLoader.getResourceAsStream(resource)
        //        new ClassReader(is).accept(printer, 0)


        //        val testClass = myClassLoader.defineClass("Test", byte)

        val testClass = loadTestClass(clazz)
        val testVClass = loadVTestClass(clazz)

        val vresult: List[TOpt[String]] = executeV(testVClass, m.myName)
        val bruteForceResult = executeBruteForce(testClass, m.myName, configOptions)
        println("expected " + bruteForceResult.reverse)
        println("found    " + vresult.reverse)

        compare(bruteForceResult.reverse, vresult.reverse)

        benchmark(testVClass, testClass, m.myName, configOptions)
    }

    def getConfigOptions(m: MyMethodNode): Set[String] = {
        val configOptions: Set[String] =
            (for (block <- m.body.blocks; instr <- block.instr; if instr.isInstanceOf[InstrLoadConfig]) yield instr.asInstanceOf[InstrLoadConfig].config).toSet
        configOptions
    }

    private def compare(expected: List[TOpt[String]], result: List[TOpt[String]]): Unit = {
        def compareOne(ctx: FeatureExpr, e: String, f: String): Unit =
            assert(ctx.isContradiction || e.trim == f.trim, s"mismatch between expected output and actual output in context $ctx: \nEXPECTED:\n$e\nFOUND:\n$f\nALL:\n" + render(result))

        val allExpected = ConditionalLib.explodeOptList(expected).map(_.mkString)
        val allFound = ConditionalLib.explodeOptList(result).map(_.mkString)
        ConditionalLib.vmapCombination(allExpected, allFound, FeatureExprFactory.True, compareOne)
    }

    private def render(result: List[TOpt[String]]): String =
        result.map(o => "[#condition " + o.feature + "]" + o.entry).mkString

    def executeBruteForce(testClass: Class[_], method: String, configOptions: Set[String]) = {
        //now all the separate executions
        var bruteForceResult: List[Opt[String]] = Nil
        val configs = explode(configOptions.toList)
        for ((sel, desel) <- configs) {
            TestOutput.output = Nil
            Config.configValues = (sel.map(_ -> 1) ++ desel.map(_ -> 0)).toMap
            val config = (sel.map(FeatureExprFactory.createDefinedExternal(_))
                ++ desel.map(FeatureExprFactory.createDefinedExternal(_).not)).
                fold(FeatureExprFactory.True)(_ and _)

            val testObject = testClass.newInstance()
            testClass.getMethod(method).invoke(testObject)

            bruteForceResult = TestOutput.output.map(_.and(config)) ++ bruteForceResult
        }
        bruteForceResult
    }

    def executeV(testVClass: Class[_], method: String): List[TOpt[String]] = {
        //VExecution first
        TestOutput.output = Nil
        Config.configValues = Map()
        val testVObject = testVClass.newInstance()
        val ctx = FeatureExprFactory.True
        testVClass.getMethod(method, classOf[FeatureExpr]).invoke(testVObject, ctx)
        val vresult = TestOutput.output
        vresult
    }

    type Feature = String
    type Config = (List[Feature], List[Feature])

    def explode(fs: List[Feature]): List[Config] = {
        if (fs.isEmpty) List((Nil, Nil))
        else if (fs.size == 1) List((List(fs.head), Nil), (Nil, List(fs.head)))
        else {
            val r = explode(fs.tail)
            r.map(x => (fs.head :: x._1, x._2)) ++ r.map(x => (x._1, fs.head :: x._2))
        }
    }


    def benchmark(testVClass: Class[_], testClass: Class[_], method: String, configOptions: Set[String]): Unit = {
        import org.scalameter._

        //measure V execution
        var testObject: Any = null
        var _ctx: FeatureExpr = null
        val vtime = config(
            Key.exec.benchRuns -> 20
        ) withWarmer {
            new Warmer.Default
            //        } withMeasurer {
            //            new Measurer.IgnoringGC
        } setUp { _ =>
            TestOutput.output = Nil
            Config.configValues = Map()
            testObject = testVClass.newInstance()
            _ctx = FeatureExprFactory.True
        } measure {
            testVClass.getMethod(method, classOf[FeatureExpr]).invoke(testObject, _ctx)
        }
        //        println(s"Total time V: $time")


        //measure brute-force execution
        val configs = explode(configOptions.toList)
        val bftimes = for ((sel, desel) <- configs)
            yield config(
                Key.exec.benchRuns -> 20
            ) withWarmer {
                new Warmer.Default
                //        } withMeasurer {
                //            new Measurer.IgnoringGC
            } setUp { _ =>
                TestOutput.output = Nil
                Config.configValues = (sel.map((_ -> 1)) ++ desel.map((_ -> 0))).toMap
                testObject = testClass.newInstance()
            } measure {
                testClass.getMethod(method).invoke(testObject)
            }


        val avgTime = bftimes.map(_.value).sum / bftimes.size
        println("VExecution time: " + vtime)
        println("Execution time: " + avgTime + bftimes.mkString(" (", ",", ")"))
        println("Slowdown: " + vtime.value / avgTime)

    }


}
