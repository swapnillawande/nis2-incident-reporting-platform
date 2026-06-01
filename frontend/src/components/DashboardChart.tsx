import { useEffect, useRef } from "react";
import type Highcharts from "highcharts";

interface DashboardChartProps {
  options: Highcharts.Options;
}

function DashboardChart({ options }: DashboardChartProps) {
  const chartRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!chartRef.current) {
      return;
    }

    let chart: Highcharts.Chart | null = null;
    let isMounted = true;

    import("highcharts").then(({ default: Highcharts }) => {
      if (!isMounted || !chartRef.current) {
        return;
      }

      chart = Highcharts.chart(chartRef.current, {
        credits: {
          enabled: false,
        },
        accessibility: {
          enabled: false,
        },
        chart: {
          backgroundColor: "transparent",
          style: {
            fontFamily: "Inter, system-ui, sans-serif",
          },
        },
        title: {
          text: undefined,
        },
        ...options,
      });
    });

    return () => {
      isMounted = false;
      chart?.destroy();
    };
  }, [options]);

  return <div className="dashboard-chart" ref={chartRef} />;
}

export default DashboardChart;
