import {
  afterNextRender,
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  viewChild,
} from '@angular/core';
import { BarChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { type ECharts, type EChartsCoreOption, init, use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';

use([BarChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

@Component({
  selector: 'app-report-chart',
  template: '<div #plot class="plot" role="img" [attr.aria-label]="label()"></div>',
  styleUrl: './report-chart.css',
})
export class ReportChart {
  readonly option = input.required<EChartsCoreOption>();
  readonly label = input.required<string>();

  private readonly plot = viewChild.required<ElementRef<HTMLElement>>('plot');
  private readonly destroyRef = inject(DestroyRef);
  private chart?: ECharts;

  constructor() {
    afterNextRender(() => {
      const element = this.plot().nativeElement;
      if (!document.createElement('canvas').getContext('2d')) {
        return;
      }
      this.chart = init(element);
      this.chart.setOption(this.option());
      const onResize = (): void => this.chart?.resize();
      window.addEventListener('resize', onResize);
      this.destroyRef.onDestroy(() => {
        window.removeEventListener('resize', onResize);
        this.chart?.dispose();
      });
    });

    effect(() => {
      const option = this.option();
      this.chart?.setOption(option, true);
    });
  }
}
