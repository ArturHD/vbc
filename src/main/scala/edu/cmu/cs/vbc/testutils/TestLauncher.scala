package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}

/**
  * Launch JUnit test cases
  *
  * For example, this can be used to launch test cases from Defects4J
  */
object TestLauncher extends App {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  // turn this two into parameters
  val testClasspath = "/Users/chupanw/Projects/Data/defects4j-math/1f/target/test-classes/"
  val mainClasspath = "/Users/chupanw/Projects/Data/defects4j-math/1f/target/classes/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, mainClasspath, testClasspath)

//  val allTests = testLoader.findTestClassFiles()
//  allTests.foreach {x =>
//    val testClass = TestClass(testLoader.loadClass(x))
//    testClass.runTests()
//  }

  val tests: List[String] = List(
//    "org.apache.commons.math3.PerfTestUtils",
//    "org.apache.commons.math3.RetryRunner",
//    "org.apache.commons.math3.RetryRunnerTest",
//    "org.apache.commons.math3.TestUtils",
//    "org.apache.commons.math3.Retry",
//    "org.apache.commons.math3.ExtendedFieldElementAbstractTest",
//    "org.apache.commons.math3.primes.PrimesTest",
//    "org.apache.commons.math3.fitting.GaussianFitterTest",
    "org.apache.commons.math3.fitting.CurveFitterTest",
//    "org.apache.commons.math3.fitting.HarmonicFitterTest",
//    "org.apache.commons.math3.fitting.PolynomialFitterTest",
//    "org.apache.commons.math3.fitting.GaussianCurveFitterTest",
//    "org.apache.commons.math3.fitting.WeightedObservedPointsTest",
//    "org.apache.commons.math3.fitting.leastsquares.CircleVectorial",
//    "org.apache.commons.math3.fitting.leastsquares.AbstractLeastSquaresOptimizerAbstractTest",
//    "org.apache.commons.math3.fitting.leastsquares.RandomCirclePointGenerator",
//    "org.apache.commons.math3.fitting.leastsquares.AbstractLeastSquaresOptimizerTest",
//    "org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizerTest",
//    "org.apache.commons.math3.fitting.leastsquares.AbstractLeastSquaresOptimizerTestValidation",
//    "org.apache.commons.math3.fitting.leastsquares.StatisticalReferenceDatasetFactory",
//    "org.apache.commons.math3.fitting.leastsquares.StatisticalReferenceDataset",
//    "org.apache.commons.math3.fitting.leastsquares.StraightLineProblem",
//    "org.apache.commons.math3.fitting.leastsquares.MinpackTest",
//    "org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizerTest",
//    "org.apache.commons.math3.fitting.leastsquares.RandomStraightLinePointGenerator",
//    "org.apache.commons.math3.fitting.leastsquares.CircleProblem",
//    "org.apache.commons.math3.analysis.QuinticFunction",
//    "org.apache.commons.math3.analysis.FunctionUtilsTest",
//    "org.apache.commons.math3.analysis.XMinus5Function",
//    "org.apache.commons.math3.analysis.SumSincFunction",
//    "org.apache.commons.math3.analysis.MonitoredFunction",
//    "org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.RombergIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.TrapezoidIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.LegendreGaussIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.SimpsonIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.MidPointIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.gauss.DummyRuleFactory",
//    "org.apache.commons.math3.analysis.integration.gauss.GaussIntegratorTest",
//    "org.apache.commons.math3.analysis.integration.gauss.HermiteParametricTest",
//    "org.apache.commons.math3.analysis.integration.gauss.HermiteTest",
//    "org.apache.commons.math3.analysis.integration.gauss.LegendreParametricTest",
//    "org.apache.commons.math3.analysis.integration.gauss.BaseRuleFactoryTest",
//    "org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionParametricTest",
//    "org.apache.commons.math3.analysis.integration.gauss.LegendreTest",
//    "org.apache.commons.math3.analysis.integration.gauss.RuleBuilder",
//    "org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionTest",
//    "org.apache.commons.math3.analysis.integration.gauss.GaussianQuadratureAbstractTest",
//    "org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolverTest",
//    "org.apache.commons.math3.analysis.solvers.MullerSolver2Test",
//    "org.apache.commons.math3.analysis.solvers.NewtonSolverTest",
//    "org.apache.commons.math3.analysis.solvers.RegulaFalsiSolverTest",
//    "org.apache.commons.math3.analysis.solvers.SecantSolverTest",
//    "org.apache.commons.math3.analysis.solvers.PegasusSolverTest",
//    "org.apache.commons.math3.analysis.solvers.MullerSolverTest",
//    "org.apache.commons.math3.analysis.solvers.BaseSecantSolverAbstractTest",
//    "org.apache.commons.math3.analysis.solvers.BrentSolverTest",
//    "org.apache.commons.math3.analysis.solvers.BisectionSolverTest",
//    "org.apache.commons.math3.analysis.solvers.LaguerreSolverTest",
//    "org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolverTest",
//    "org.apache.commons.math3.analysis.solvers.IllinoisSolverTest",
//    "org.apache.commons.math3.analysis.solvers.RiddersSolverTest",
//    "org.apache.commons.math3.analysis.solvers.UnivariateSolverUtilsTest",
//    "org.apache.commons.math3.analysis.function.SincTest",
//    "org.apache.commons.math3.analysis.function.HarmonicOscillatorTest",
//    "org.apache.commons.math3.analysis.function.SqrtTest",
//    "org.apache.commons.math3.analysis.function.LogisticTest",
//    "org.apache.commons.math3.analysis.function.SigmoidTest",
//    "org.apache.commons.math3.analysis.function.GaussianTest",
//    "org.apache.commons.math3.analysis.function.StepFunctionTest",
//    "org.apache.commons.math3.analysis.function.LogitTest",
//    "org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiatorTest",
//    "org.apache.commons.math3.analysis.differentiation.JacobianFunctionTest",
//    "org.apache.commons.math3.analysis.differentiation.DSCompilerTest",
//    "org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest",
//    "org.apache.commons.math3.analysis.differentiation.GradientFunctionTest",
//    "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionTest",
//    "org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest",
//    "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeFormTest",
//    "org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunctionTest",
//    "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonFormTest",
//    "org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest",
//    "org.apache.commons.math3.analysis.interpolation.UnivariatePeriodicInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.NevilleInterpolatorTest",
    "org.apache.commons.math3.analysis.interpolation.MicrosphereInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.HermiteInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest",
//    "org.apache.commons.math3.analysis.interpolation.LoessInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.SplineInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.LinearInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolatorTest",
//    "org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatorTest",
//    "org.apache.commons.math3.util.IncrementorTest",
//    "org.apache.commons.math3.util.OpenIntToDoubleHashMapTest",
//    "org.apache.commons.math3.util.ContinuedFractionTest",
//    "org.apache.commons.math3.util.CombinatoricsUtilsTest",
//    "org.apache.commons.math3.util.DoubleArrayAbstractTest",
//    "org.apache.commons.math3.util.BigRealFieldTest",
//    "org.apache.commons.math3.util.DefaultTransformerTest",
//    "org.apache.commons.math3.util.MultidimensionalCounterTest",
//    "org.apache.commons.math3.util.FastMathStrictComparisonTest",
//    "org.apache.commons.math3.util.ResizableDoubleArrayTest",
//    "org.apache.commons.math3.util.ArithmeticUtilsTest",
//    "org.apache.commons.math3.util.BigRealTest",
//    "org.apache.commons.math3.util.FastMathTest",
//    "org.apache.commons.math3.util.Decimal64Test",
//    "org.apache.commons.math3.util.PairTest",
//    "org.apache.commons.math3.util.OpenIntToFieldTest",
//    "org.apache.commons.math3.util.MathUtilsTest",
//    "org.apache.commons.math3.util.FastMathTestPerformance",
//    "org.apache.commons.math3.util.PrecisionTest",
//    "org.apache.commons.math3.util.TestBean",
//    "org.apache.commons.math3.util.MathArraysTest",
//    "org.apache.commons.math3.util.TransformerMapTest",
//    "org.apache.commons.math3.complex.ComplexFormatAbstractTest",
//    "org.apache.commons.math3.complex.ComplexFormatTest",
//    "org.apache.commons.math3.complex.ComplexFieldTest",
//    "org.apache.commons.math3.complex.RootsOfUnityTest",
//    "org.apache.commons.math3.complex.ComplexUtilsTest",
//    "org.apache.commons.math3.complex.ComplexTest",
//    "org.apache.commons.math3.complex.FrenchComplexFormatTest",
//    "org.apache.commons.math3.complex.QuaternionTest",
//    "org.apache.commons.math3.optimization.MultivariateDifferentiableVectorMultiStartOptimizerTest",
//    "org.apache.commons.math3.optimization.MultivariateDifferentiableMultiStartOptimizerTest",
//    "org.apache.commons.math3.optimization.SimpleVectorValueCheckerTest",
//    "org.apache.commons.math3.optimization.SimplePointCheckerTest",
//    "org.apache.commons.math3.optimization.PointValuePairTest",
//    "org.apache.commons.math3.optimization.SimpleValueCheckerTest",
//    "org.apache.commons.math3.optimization.MultivariateMultiStartOptimizerTest",
//    "org.apache.commons.math3.optimization.PointVectorValuePairTest",
    "org.apache.commons.math3.optimization.fitting.GaussianFitterTest",
//    "org.apache.commons.math3.optimization.fitting.CurveFitterTest",
//    "org.apache.commons.math3.optimization.fitting.HarmonicFitterTest",
//    "org.apache.commons.math3.optimization.fitting.PolynomialFitterTest",
//    "org.apache.commons.math3.optimization.direct.MultivariateFunctionPenaltyAdapterTest",
//    "org.apache.commons.math3.optimization.direct.SimplexOptimizerMultiDirectionalTest",
//    "org.apache.commons.math3.optimization.direct.BOBYQAOptimizerTest",
//    "org.apache.commons.math3.optimization.direct.CMAESOptimizerTest",
//    "org.apache.commons.math3.optimization.direct.SimplexOptimizerNelderMeadTest",
//    "org.apache.commons.math3.optimization.direct.PowellOptimizerTest",
//    "org.apache.commons.math3.optimization.direct.MultivariateFunctionMappingAdapterTest",
//    "org.apache.commons.math3.optimization.general.CircleVectorial",
//    "org.apache.commons.math3.optimization.general.DummyOptimizer",
//    "org.apache.commons.math3.optimization.general.AbstractLeastSquaresOptimizerAbstractTest",
//    "org.apache.commons.math3.optimization.general.RandomCirclePointGenerator",
//    "org.apache.commons.math3.optimization.general.AbstractLeastSquaresOptimizerTest",
//    "org.apache.commons.math3.optimization.general.GaussNewtonOptimizerTest",
//    "org.apache.commons.math3.optimization.general.AbstractLeastSquaresOptimizerTestValidation",
//    "org.apache.commons.math3.optimization.general.StatisticalReferenceDatasetFactory",
//    "org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizerTest",
//    "org.apache.commons.math3.optimization.general.StatisticalReferenceDataset",
//    "org.apache.commons.math3.optimization.general.StraightLineProblem",
//    "org.apache.commons.math3.optimization.general.MinpackTest",
//    "org.apache.commons.math3.optimization.general.CircleScalar",
    "org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizerTest",
    "org.apache.commons.math3.optimization.general.RandomStraightLinePointGenerator",
    "org.apache.commons.math3.optimization.general.CircleProblem"
//     // phase 2
//    "org.apache.commons.math3.optimization.linear.SimplexSolverTest",
//    "org.apache.commons.math3.optimization.linear.SimplexTableauTest",
//    "org.apache.commons.math3.optimization.univariate.SimpleUnivariateValueCheckerTest",
//    "org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizerTest",
//    "org.apache.commons.math3.optimization.univariate.BracketFinderTest",
//    "org.apache.commons.math3.optimization.univariate.BrentOptimizerTest",
//    "org.apache.commons.math3.linear.RRQRDecompositionTest",
//    "org.apache.commons.math3.linear.MatrixUtilsTest",
//    "org.apache.commons.math3.linear.SymmLQTest",
//    "org.apache.commons.math3.linear.QRSolverTest",
//    "org.apache.commons.math3.linear.SparseFieldVectorTest",
//    "org.apache.commons.math3.linear.RectangularCholeskyDecompositionTest",
//    "org.apache.commons.math3.linear.BlockFieldMatrixTest",
//    "org.apache.commons.math3.linear.FieldLUDecompositionTest",
//    "org.apache.commons.math3.linear.CholeskySolverTest",
//    "org.apache.commons.math3.linear.EigenDecompositionTest",
//    "org.apache.commons.math3.linear.SparseRealMatrixTest",
//    "org.apache.commons.math3.linear.EigenSolverTest",
//    "org.apache.commons.math3.linear.QRDecompositionTest",
//    "org.apache.commons.math3.linear.InverseHilbertMatrix",
//    "org.apache.commons.math3.linear.LUDecompositionTest",
//    "org.apache.commons.math3.linear.FrenchRealVectorFormatTest",
//    "org.apache.commons.math3.linear.SchurTransformerTest",
//    "org.apache.commons.math3.linear.FieldLUSolverTest",
//    "org.apache.commons.math3.linear.SingularValueDecompositionTest",
//    "org.apache.commons.math3.linear.ConjugateGradientTest",
//    "org.apache.commons.math3.linear.ArrayFieldVectorTest",
//    "org.apache.commons.math3.linear.BlockRealMatrixTest",
//    "org.apache.commons.math3.linear.OpenMapRealMatrixTest",
//    "org.apache.commons.math3.linear.RealVectorAbstractTest",
//    "org.apache.commons.math3.linear.SparseFieldMatrixTest",
//    "org.apache.commons.math3.linear.UnmodifiableRealVectorAbstractTest",
//    "org.apache.commons.math3.linear.RealVectorTest",
//    "org.apache.commons.math3.linear.Array2DRowRealMatrixTest",
//    "org.apache.commons.math3.linear.LUSolverTest",
//    "org.apache.commons.math3.linear.DiagonalMatrixTest",
//    "org.apache.commons.math3.linear.SingularValueSolverTest",
//    "org.apache.commons.math3.linear.RealVectorFormatAbstractTest",
//    "org.apache.commons.math3.linear.SparseRealVectorTest",
//    "org.apache.commons.math3.linear.HilbertMatrix",
//    "org.apache.commons.math3.linear.ArrayRealVectorTest",
//    "org.apache.commons.math3.linear.RealMatrixFormatTest",
//    "org.apache.commons.math3.linear.UnmodifiableArrayRealVectorTest",
//    "org.apache.commons.math3.linear.FieldMatrixImplTest",
//    "org.apache.commons.math3.linear.CholeskyDecompositionTest",
//    "org.apache.commons.math3.linear.HessenbergTransformerTest",
//    "org.apache.commons.math3.linear.MatrixDimensionMismatchExceptionTest",
//    "org.apache.commons.math3.linear.RealVectorFormatTest",
//    "org.apache.commons.math3.linear.UnmodifiableOpenMapRealVectorTest",
//    "org.apache.commons.math3.linear.RealMatrixFormatAbstractTest",
//    "org.apache.commons.math3.linear.BiDiagonalTransformerTest",
//    "org.apache.commons.math3.linear.RRQRSolverTest",
//    "org.apache.commons.math3.linear.TriDiagonalTransformerTest",
//    "org.apache.commons.math3.distribution.LevyDistributionTest",
//    "org.apache.commons.math3.distribution.UniformRealDistributionTest",
//    "org.apache.commons.math3.distribution.BetaDistributionTest",
//    "org.apache.commons.math3.distribution.TriangularDistributionTest",
//    "org.apache.commons.math3.distribution.TDistributionTest",
//    "org.apache.commons.math3.distribution.MultivariateNormalDistributionTest",
//    "org.apache.commons.math3.distribution.WeibullDistributionTest",
//    "org.apache.commons.math3.distribution.ExponentialDistributionTest",
//    "org.apache.commons.math3.distribution.LogNormalDistributionTest",
//    "org.apache.commons.math3.distribution.HypergeometricDistributionTest",
//    "org.apache.commons.math3.distribution.PascalDistributionTest",
//    "org.apache.commons.math3.distribution.UniformIntegerDistributionTest",
//    "org.apache.commons.math3.distribution.BinomialDistributionTest",
//    "org.apache.commons.math3.distribution.CauchyDistributionTest",
//    "org.apache.commons.math3.distribution.PoissonDistributionTest",
//    "org.apache.commons.math3.distribution.ZipfDistributionTest",
//    "org.apache.commons.math3.distribution.AbstractIntegerDistributionTest",
//    "org.apache.commons.math3.distribution.ChiSquaredDistributionTest",
//    "org.apache.commons.math3.distribution.EnumeratedRealDistributionTest",
//    "org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest",
//    "org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistribution",
//    "org.apache.commons.math3.distribution.NormalDistributionTest",
//    "org.apache.commons.math3.distribution.ParetoDistributionTest",
//    "org.apache.commons.math3.distribution.FDistributionTest",
//    "org.apache.commons.math3.distribution.GammaDistributionTest",
//    "org.apache.commons.math3.distribution.AbstractRealDistributionTest",
//    "org.apache.commons.math3.distribution.EnumeratedIntegerDistributionTest",
//    "org.apache.commons.math3.distribution.IntegerDistributionAbstractTest",
//    "org.apache.commons.math3.distribution.RealDistributionAbstractTest",
//    "org.apache.commons.math3.distribution.KolmogorovSmirnovDistributionTest",
//    "org.apache.commons.math3.distribution.GeometricDistributionTest",
//    "org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest",
//    "org.apache.commons.math3.optim.SimpleVectorValueCheckerTest",
//    "org.apache.commons.math3.optim.SimplePointCheckerTest",
//    "org.apache.commons.math3.optim.PointValuePairTest",
//    "org.apache.commons.math3.optim.SimpleValueCheckerTest",
//    "org.apache.commons.math3.optim.PointVectorValuePairTest",
//    "org.apache.commons.math3.optim.linear.SimplexSolverTest",
//    "org.apache.commons.math3.optim.linear.SimplexTableauTest",
//    "org.apache.commons.math3.optim.univariate.SimpleUnivariateValueCheckerTest",
//    "org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest",
//    "org.apache.commons.math3.optim.univariate.BracketFinderTest",
//    "org.apache.commons.math3.optim.univariate.BrentOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapterTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.gradient.CircleScalar",
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.MultiStartMultivariateVectorOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.CircleVectorial",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.DummyOptimizer",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.AbstractLeastSquaresOptimizerAbstractTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.RandomCirclePointGenerator",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.AbstractLeastSquaresOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.AbstractLeastSquaresOptimizerTestValidation",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.StatisticalReferenceDatasetFactory",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.StatisticalReferenceDataset",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.StraightLineProblem",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.RandomStraightLinePointGenerator",
//    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.CircleProblem",
//    "org.apache.commons.math3.userguide.ExampleUtils",
//    "org.apache.commons.math3.userguide.LowDiscrepancyGeneratorComparison",
//    "org.apache.commons.math3.userguide.ClusterAlgorithmComparison",
//    "org.apache.commons.math3.special.ErfTest",
//    "org.apache.commons.math3.special.GammaTest",
//    "org.apache.commons.math3.special.BetaTest",
//    "org.apache.commons.math3.ode.TestProblem2",
//    "org.apache.commons.math3.ode.FirstOrderConverterTest",
//    "org.apache.commons.math3.ode.TestProblem4",
//    "org.apache.commons.math3.ode.TestProblem6",
//    "org.apache.commons.math3.ode.TestProblem1",
//    "org.apache.commons.math3.ode.TestProblem3",
//    "org.apache.commons.math3.ode.TestProblem5",
//    "org.apache.commons.math3.ode.TestProblemHandler",
//    "org.apache.commons.math3.ode.ContinuousOutputModelTest",
//    "org.apache.commons.math3.ode.TestProblemAbstract",
//    "org.apache.commons.math3.ode.JacobianMatricesTest",
//    "org.apache.commons.math3.ode.TestProblemFactory",
//    "org.apache.commons.math3.ode.sampling.StepInterpolatorTestUtils",
//    "org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest",
//    "org.apache.commons.math3.ode.sampling.StepNormalizerOutputTestBase",
//    "org.apache.commons.math3.ode.sampling.StepNormalizerOutputTest",
//    "org.apache.commons.math3.ode.sampling.DummyStepInterpolatorTest",
//    "org.apache.commons.math3.ode.sampling.StepNormalizerOutputOverlapTest",
//    "org.apache.commons.math3.ode.sampling.StepNormalizerTest",
//    "org.apache.commons.math3.ode.sampling.DummyStepInterpolator",
//    "org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.GillIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.DormandPrince54StepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerStepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.ThreeEighthesStepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.MidpointStepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.HighamHall54StepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.EulerStepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.DormandPrince853StepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.DormandPrince54IntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.MidpointIntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaStepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.HighamHall54IntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.StepProblem",
//    "org.apache.commons.math3.ode.nonstiff.GillStepInterpolatorTest",
//    "org.apache.commons.math3.ode.nonstiff.DormandPrince853IntegratorTest",
//    "org.apache.commons.math3.ode.nonstiff.EulerIntegratorTest",
//    "org.apache.commons.math3.ode.events.OverlappingEventsTest",
//    "org.apache.commons.math3.ode.events.EventStateTest",
//    "org.apache.commons.math3.ode.events.EventFilterTest",
//    "org.apache.commons.math3.ode.events.ReappearingEventTest",
//    "org.apache.commons.math3.stat.CertifiedDataTest",
//    "org.apache.commons.math3.stat.StatUtilsTest",
//    "org.apache.commons.math3.stat.FrequencyTest",
//    "org.apache.commons.math3.stat.ranking.NaturalRankingTest",
//    "org.apache.commons.math3.stat.correlation.StorelessCovarianceTest",
//    "org.apache.commons.math3.stat.correlation.CovarianceTest",
//    "org.apache.commons.math3.stat.correlation.SpearmansRankCorrelationTest",
//    "org.apache.commons.math3.stat.correlation.PearsonsCorrelationTest",
//    "org.apache.commons.math3.stat.clustering.EuclideanIntegerPointTest",
//    "org.apache.commons.math3.stat.clustering.EuclideanDoublePointTest",
//    "org.apache.commons.math3.stat.clustering.KMeansPlusPlusClustererTest",
//    "org.apache.commons.math3.stat.clustering.DBSCANClustererTest",
//    "org.apache.commons.math3.stat.inference.MannWhitneyUTestTest",
//    "org.apache.commons.math3.stat.inference.TTestTest",
//    "org.apache.commons.math3.stat.inference.TestUtilsTest",
//    "org.apache.commons.math3.stat.inference.OneWayAnovaTest",
//    "org.apache.commons.math3.stat.inference.WilcoxonSignedRankTestTest",
//    "org.apache.commons.math3.stat.inference.ChiSquareTestTest",
//    "org.apache.commons.math3.stat.inference.GTestTest",
//    "org.apache.commons.math3.stat.descriptive.SummaryStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.AbstractUnivariateStatisticTest",
//    "org.apache.commons.math3.stat.descriptive.AggregateSummaryStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.StatisticalSummaryValuesTest",
//    "org.apache.commons.math3.stat.descriptive.MixedListUnivariateImplTest",
//    "org.apache.commons.math3.stat.descriptive.UnivariateStatisticAbstractTest",
//    "org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatisticAbstractTest",
//    "org.apache.commons.math3.stat.descriptive.SynchronizedMultivariateSummaryStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.ListUnivariateImplTest",
//    "org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.ListUnivariateImpl",
//    "org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatisticsTest",
//    "org.apache.commons.math3.stat.descriptive.moment.SecondMomentTest",
//    "org.apache.commons.math3.stat.descriptive.moment.KurtosisTest",
//    "org.apache.commons.math3.stat.descriptive.moment.SemiVarianceTest",
//    "org.apache.commons.math3.stat.descriptive.moment.GeometricMeanTest",
//    "org.apache.commons.math3.stat.descriptive.moment.ThirdMomentTest",
//    "org.apache.commons.math3.stat.descriptive.moment.StandardDeviationTest",
//    "org.apache.commons.math3.stat.descriptive.moment.SkewnessTest",
//    "org.apache.commons.math3.stat.descriptive.moment.MeanTest",
//    "org.apache.commons.math3.stat.descriptive.moment.FourthMomentTest",
//    "org.apache.commons.math3.stat.descriptive.moment.FirstMomentTest",
//    "org.apache.commons.math3.stat.descriptive.moment.VarianceTest",
//    "org.apache.commons.math3.stat.descriptive.moment.VectorialMeanTest",
//    "org.apache.commons.math3.stat.descriptive.moment.VectorialCovarianceTest",
//    "org.apache.commons.math3.stat.descriptive.moment.InteractionTest",
//    "org.apache.commons.math3.stat.descriptive.rank.MinTest",
//    "org.apache.commons.math3.stat.descriptive.rank.MedianTest",
//    "org.apache.commons.math3.stat.descriptive.rank.MaxTest",
//    "org.apache.commons.math3.stat.descriptive.rank.PercentileTest",
//    "org.apache.commons.math3.stat.descriptive.summary.SumLogTest",
//    "org.apache.commons.math3.stat.descriptive.summary.SumTest",
//    "org.apache.commons.math3.stat.descriptive.summary.ProductTest",
//    "org.apache.commons.math3.stat.descriptive.summary.SumSqTest",
//    "org.apache.commons.math3.stat.regression.OLSMultipleLinearRegressionTest",
//    "org.apache.commons.math3.stat.regression.MillerUpdatingRegressionTest",
//    "org.apache.commons.math3.stat.regression.MultipleLinearRegressionAbstractTest",
//    "org.apache.commons.math3.stat.regression.SimpleRegressionTest",
//    "org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest",
//    "org.apache.commons.math3.stat.data.LotteryTest",
//    "org.apache.commons.math3.stat.data.LewTest",
//    "org.apache.commons.math3.stat.data.CertifiedDataAbstractTest",
//    "org.apache.commons.math3.ml.distance.EarthMoversDistanceTest",
//    "org.apache.commons.math3.ml.distance.ChebyshevDistanceTest",
//    "org.apache.commons.math3.ml.distance.ManhattanDistanceTest",
//    "org.apache.commons.math3.ml.distance.CanberraDistanceTest",
//    "org.apache.commons.math3.ml.distance.EuclideanDistanceTest",
//    "org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClustererTest",
//    "org.apache.commons.math3.ml.clustering.FuzzyKMeansClustererTest",
//    "org.apache.commons.math3.ml.clustering.KMeansPlusPlusClustererTest",
//    "org.apache.commons.math3.ml.clustering.DBSCANClustererTest",
//    "org.apache.commons.math3.dfp.DfpTest",
//    "org.apache.commons.math3.dfp.Decimal10",
//    "org.apache.commons.math3.dfp.DfpMathTest",
//    "org.apache.commons.math3.dfp.DfpDecTest",
//    "org.apache.commons.math3.dfp.BracketingNthOrderBrentSolverDFPTest",
//    "org.apache.commons.math3.genetics.GeneticAlgorithmTestBinary",
//    "org.apache.commons.math3.genetics.RandomKeyTest",
//    "org.apache.commons.math3.genetics.ListPopulationTest",
//    "org.apache.commons.math3.genetics.ChromosomeTest",
//    "org.apache.commons.math3.genetics.ElitisticListPopulationTest",
//    "org.apache.commons.math3.genetics.FixedElapsedTimeTest",
//    "org.apache.commons.math3.genetics.DummyRandomKey",
//    "org.apache.commons.math3.genetics.TournamentSelectionTest",
//    "org.apache.commons.math3.genetics.DummyListChromosome",
//    "org.apache.commons.math3.genetics.BinaryChromosomeTest",
//    "org.apache.commons.math3.genetics.GeneticAlgorithmTestPermutations",
//    "org.apache.commons.math3.genetics.OnePointCrossoverTest",
//    "org.apache.commons.math3.genetics.FitnessCachingTest",
//    "org.apache.commons.math3.genetics.OrderedCrossoverTest",
//    "org.apache.commons.math3.genetics.UniformCrossoverTest",
//    "org.apache.commons.math3.genetics.RandomKeyMutationTest",
//    "org.apache.commons.math3.genetics.NPointCrossoverTest",
//    "org.apache.commons.math3.genetics.DummyBinaryChromosome",
//    "org.apache.commons.math3.genetics.BinaryMutationTest",
//    "org.apache.commons.math3.genetics.FixedGenerationCountTest",
//    "org.apache.commons.math3.genetics.CycleCrossoverTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.LineTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.RotationTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinatesTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.Vector3DFormatAbstractTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.FrenchVector3DFormatTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.Vector3DFormatTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.SubLineTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.RotationOrderTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.PlaneTest",
//    "org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest",
//    "org.apache.commons.math3.geometry.euclidean.oned.IntervalTest",
//    "org.apache.commons.math3.geometry.euclidean.oned.IntervalsSetTest",
//    "org.apache.commons.math3.geometry.euclidean.twod.SegmentTest",
//    "org.apache.commons.math3.geometry.euclidean.twod.LineTest",
//    "org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest",
//    "org.apache.commons.math3.geometry.euclidean.twod.SubLineTest",
//    "org.apache.commons.math3.geometry.partitioning.utilities.AVLTreeTest",
//    "org.apache.commons.math3.filter.KalmanFilterTest",
//    "org.apache.commons.math3.random.GaussianRandomGeneratorTest",
//    "org.apache.commons.math3.random.RandomAdaptorTest",
//    "org.apache.commons.math3.random.Well19937cTest",
//    "org.apache.commons.math3.random.UniformRandomGeneratorTest",
//    "org.apache.commons.math3.random.ValueServerTest",
//    "org.apache.commons.math3.random.AbstractRandomGeneratorTest",
//    "org.apache.commons.math3.random.ISAACTest",
//    "org.apache.commons.math3.random.RandomDataGeneratorTest",
//    "org.apache.commons.math3.random.EmpiricalDistributionTest",
//    "org.apache.commons.math3.random.UncorrelatedRandomVectorGeneratorTest",
//    "org.apache.commons.math3.random.Well44497bTest",
//    "org.apache.commons.math3.random.UnitSphereRandomVectorGeneratorTest",
//    "org.apache.commons.math3.random.BitsStreamGeneratorTest",
//    "org.apache.commons.math3.random.Well19937aTest",
//    "org.apache.commons.math3.random.Well512aTest",
//    "org.apache.commons.math3.random.Well44497aTest",
//    "org.apache.commons.math3.random.RandomGeneratorFactoryTest",
//    "org.apache.commons.math3.random.StableRandomGeneratorTest",
//    "org.apache.commons.math3.random.SynchronizedRandomGeneratorTest",
//    "org.apache.commons.math3.random.CorrelatedRandomVectorGeneratorTest",
//    "org.apache.commons.math3.random.SobolSequenceGeneratorTest",
//    "org.apache.commons.math3.random.MersenneTwisterTest",
//    "org.apache.commons.math3.random.HaltonSequenceGeneratorTest",
//    "org.apache.commons.math3.random.Well1024aTest",
//    "org.apache.commons.math3.random.RandomGeneratorAbstractTest",
//    "org.apache.commons.math3.random.TestRandomGenerator",
//    "org.apache.commons.math3.fraction.BigFractionFormatTest",
//    "org.apache.commons.math3.fraction.BigFractionTest",
//    "org.apache.commons.math3.fraction.FractionFieldTest",
//    "org.apache.commons.math3.fraction.FractionTest",
//    "org.apache.commons.math3.fraction.FractionFormatTest",
//    "org.apache.commons.math3.fraction.BigFractionFieldTest",
//    "org.apache.commons.math3.exception.DimensionMismatchExceptionTest",
//    "org.apache.commons.math3.exception.NonMonotonicSequenceExceptionTest",
//    "org.apache.commons.math3.exception.TooManyEvaluationsExceptionTest",
//    "org.apache.commons.math3.exception.OutOfRangeExceptionTest",
//    "org.apache.commons.math3.exception.NotPositiveExceptionTest",
//    "org.apache.commons.math3.exception.NotStrictlyPositiveExceptionTest",
//    "org.apache.commons.math3.exception.NumberIsTooSmallExceptionTest",
//    "org.apache.commons.math3.exception.NumberIsTooLargeExceptionTest",
//    "org.apache.commons.math3.exception.MaxCountExceededExceptionTest",
//    "org.apache.commons.math3.exception.util.ExceptionContextTest",
//    "org.apache.commons.math3.exception.util.ArgUtilsTest",
//    "org.apache.commons.math3.exception.util.LocalizedFormatsTest",
//    "org.apache.commons.math3.transform.FastFourierTransformerTest",
//    "org.apache.commons.math3.transform.FastHadamardTransformerTest",
//    "org.apache.commons.math3.transform.RealTransformerAbstractTest",
//    "org.apache.commons.math3.transform.FastCosineTransformerTest",
//    "org.apache.commons.math3.transform.FastSineTransformerTest"
  )

    tests.foreach {x =>
      val testClass = TestClass(testLoader.loadClass(x))
      testClass.runTests()
    }

  TestStat.printToConsole()
}
