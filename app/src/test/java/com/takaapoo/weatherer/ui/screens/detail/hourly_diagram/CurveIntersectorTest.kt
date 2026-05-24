package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.ui.geometry.Offset
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CurveIntersectorTest {
    @Test
    fun `cubicCurveXtoY linear data`() {
        val data = listOf(1f, 2f, 3f, 4f)
        val controlPoints1 = listOf(Offset(1/3f, 4/3f), Offset(1/3f, 7/3f),
            Offset(1/3f, 10/3f))
        val controlPoints2 = listOf(Offset(2/3f, 5/3f), Offset(2/3f, 8/3f),
            Offset(2/3f, 11/3f))

        val realValue = cubicCurveXtoY(
            data = data,
            controlPoints1 = controlPoints1,
            controlPoints2 = controlPoints2,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 1.5f,
        )
        val expectedValue = 2.5f
        assertThat(realValue).isWithin(0.001f).of(expectedValue)
    }

    @Test
    fun `cubicCurveXtoY null value in controlPoints1`() {
        val data = listOf(1f, 2f, 3f, 4f)
        val controlPoints1 = listOf(Offset(1/3f, 4/3f), null,
            Offset(1/3f, 10/3f))
        val controlPoints2 = listOf(Offset(2/3f, 5/3f), Offset(2/3f, 8/3f),
            Offset(2/3f, 11/3f))

        val realValue = cubicCurveXtoY(
            data = data,
            controlPoints1 = controlPoints1,
            controlPoints2 = controlPoints2,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 1.5f,
        )
        assertThat(realValue).isNull()
    }

    @Test
    fun `cubicCurveXtoY targetX out of bounds`() {
        val data = listOf(1f, 2f, 3f, 4f)
        val controlPoints1 = listOf(Offset(1/3f, 4/3f), Offset(1/3f, 7/3f),
            Offset(1/3f, 10/3f))
        val controlPoints2 = listOf(Offset(2/3f, 5/3f), Offset(2/3f, 8/3f),
            Offset(2/3f, 11/3f))

        val realValue = cubicCurveXtoY(
            data = data,
            controlPoints1 = controlPoints1,
            controlPoints2 = controlPoints2,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = -0.5f,
        )
        assertThat(realValue).isNull()
    }

    @Test
    fun `cubicCurveXtoY real data`() {
        val data = listOf(3.5f, 2.8f, 4f, 4.9f, 7.1f, 6f, 1f, -4f, 3f, 6.1f, 7.8f, 9.9f, 15f, 16f,
            20.1f, 22f, 18f, 19f, 16.5f, 17f, 21f, 12f, 9.5f, 8f, 16f, 11f, 6f, 5.5f, 5f, 9f)
        val cntPnt1Y = listOf(3.077146f,2.945707f,4.340025f,5.494194f,7.483198f,4.973012f,-1.375247f,
            -3.472026f,5.26335f,6.618625f,8.26215f,11.33278f,16.00674f,16.64024f,21.63228f,21.23065f,
            17.44514f,18.98879f,15.59971f,18.61238f,19.95076f,9.584578f,8.710929f,9.571705f,17.00225f,
            8.419286f,5.320604f,5.298299f,5.4862f)
        val controlPoints1 = List(size = cntPnt1Y.size){ i ->
            Offset(1/3f, cntPnt1Y[i])
        }

        val cntPnt2Y = listOf(2.654293f,3.659975f,4.305806f,6.716802f,7.026988f,3.375247f,-4.527974f,
            0.7366498f,5.581375f,7.33785f,8.467224f,13.99326f,15.35976f,18.56772f,22.76935f,18.55486f,
            19.01121f,17.40029f,15.38762f,22.04924f,14.41542f,10.28907f,6.428295f,14.99775f,13.58071f,
            6.679396f,5.701701f,4.5138f,7.2431f)
        val controlPoints2 = List(size = cntPnt2Y.size){ i ->
            Offset(2/3f, cntPnt2Y[i])
        }
        val targetX = listOf(6.4f, 7.1f, 11.05f, 13.9f, 22.3f)
        val expectedValue = listOf(-1.9382653f, -3.7367993f, 10.1237641f, 19.6301431f, 8.5308402f)

        targetX.forEachIndexed { i,  target ->
            val realValue = cubicCurveXtoY(
                data = data,
                controlPoints1 = controlPoints1,
                controlPoints2 = controlPoints2,
                firstPointX = 0f,
                lastPointX = 29f,
                targetX = target,
            )
            assertThat(realValue).isWithin(0.001f).of(expectedValue[i])
        }
    }

    @Test
    fun `cubicCurveXtoY data with null between`() {
        val data = listOf(3.5f, 2.8f, 4f, 4.9f, 7.1f, 6f, null, -4f, 3f, 6.1f, 7.8f, 9.9f, 15f, 16f,
            20.1f, 22f, 18f, 19f, 16.5f, 17f, 21f, 12f, 9.5f, 8f, 16f, 11f, 6f, 5.5f, 5f, 9f)

        val controlPoints1 = listOf(
            Offset(1/3f,3.07859f), Offset(1/3f,2.94282f),
            Offset(1/3f,4.35013f), Offset(1/3f,5.45667f),
            Offset(1/3f,7.6232f), Offset(2/3f,2.90104f), null,
            Offset(1/3f,-2.94954f), Offset(1/3f,5.12335f),
            Offset(1/3f,6.65614f), Offset(1/3f,8.2521f),
            Offset(1/3f,11.3355f), Offset(1/3f,16.006f),
            Offset(1/3f,16.6404f), Offset(1/3f,21.6322f),
            Offset(1/3f,21.2307f), Offset(1/3f,17.4451f),
            Offset(1/3f,18.9888f), Offset(1/3f,15.5997f),
            Offset(1/3f,18.6124f), Offset(1/3f,19.9508f),
            Offset(1/3f,9.58458f), Offset(1/3f,8.71093f),
            Offset(1/3f,9.5717f), Offset(1/3f,17.0023f),
            Offset(1/3f,8.41929f), Offset(1/3f,5.3206f),
            Offset(1/3f,5.2983f), Offset(1/3f,5.4862f)
        )

        val controlPoints2 = listOf(
            Offset(2/3f, 2.65718f),  Offset(2/3f, 3.64987f),
            Offset(2/3f, 4.34333f),  Offset(2/3f, 6.5768f),
            Offset(2/3f, 7.54948f),  Offset(4/3f, -6.10093f), null,
            Offset(2/3f, 0.87665f),  Offset(2/3f, 5.54386f),
            Offset(2/3f, 7.3479f),  Offset(2/3f, 8.46453f),
            Offset(2/3f, 13.994f),  Offset(2/3f, 15.3596f),
            Offset(2/3f, 18.5678f),  Offset(2/3f, 22.7693f),
            Offset(2/3f, 18.5549f),  Offset(2/3f, 19.0112f),
            Offset(2/3f, 17.4003f),  Offset(2/3f, 15.3876f),
            Offset(2/3f, 22.0492f),  Offset(2/3f, 14.4154f),
            Offset(2/3f, 10.2891f),  Offset(2/3f, 6.4283f),
            Offset(2/3f, 14.9977f),  Offset(2/3f, 13.5807f),
            Offset(2/3f, 6.6794f),  Offset(2/3f, 5.7017f),
            Offset(2/3f, 4.5138f),  Offset(2/3f, 7.2431f)
        )

        val targetX = listOf(4.7f, 5.3f, 6.12f, 6.92f)
        val expectedValue = listOf(7.01981f, 4.26441f, -1.77329f, -4.19991f)

        targetX.forEachIndexed { i,  target ->
            val realValue = cubicCurveXtoY(
                data = data,
                controlPoints1 = controlPoints1,
                controlPoints2 = controlPoints2,
                firstPointX = 0f,
                lastPointX = 29f,
                targetX = target,
            )
            assertThat(realValue).isWithin(0.001f).of(expectedValue[i])
        }
    }

    @Test
    fun `linearCurveXtoY null value in between`() {
        val data = listOf(1f, 2f, null, 4f)
        val realValue = linearCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 2f,
        )
        assertThat(realValue).isEqualTo(3f)
    }

    @Test
    fun `linearCurveXtoY targetX out of bounds`() {
        val data = listOf(1f, 2f, null, 4f)
        val realValue = linearCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = -2f,
        )
        assertThat(realValue).isNull()
    }

    @Test
    fun `linearCurveXtoY all values null`() {
        val data = listOf(null, null, null, null)
        val realValue = linearCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 1f,
        )
        assertThat(realValue).isNull()
    }

    @Test
    fun `linearCurveXtoY all values null except one`() {
        val data = listOf(null, null, 3f, null)
        val realValue = linearCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 2f,
        )
        assertThat(realValue).isEqualTo(3f)
    }

    @Test
    fun `stepCurveXtoY normal behavior`() {
        val data = listOf(1f, 2f, null, 4f)
        val realValue = stepCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = -0.5f,
        )
        assertThat(realValue).isEqualTo(1f)
    }

    @Test
    fun `stepCurveXtoY null value in between`() {
        val data = listOf(1f, 2f, null, 4f)
        val realValue = stepCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 1.5f,
        )
        assertThat(realValue).isNull()
    }

    @Test
    fun `stepCurveXtoY all values null except one`() {
        val data = listOf(null, null, 3f, null)
        val realValue = stepCurveXtoY(
            data = data,
            firstPointX = 0f,
            lastPointX = 3f,
            targetX = 2f,
        )
        assertThat(realValue).isEqualTo(3f)
    }


}