import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ProcessService }         from '../../services/process.service';
import { ProcessExecution }       from '../../models/process-execution.model';

@Component({
  selector: 'app-execution-list',
  templateUrl: './execution-list.component.html',
  styleUrls: ['./execution-list.component.css']
})
export class ExecutionListComponent implements OnChanges {
  @Input() definitionId!: string;
  executions: ProcessExecution[] = [];
  loading = false;

  constructor(private svc: ProcessService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['definitionId'] && this.definitionId) {
      this.loadExecutions();
    }
  }

  private loadExecutions() {
    this.loading = true;
    this.svc.listExecutions(this.definitionId).subscribe(
      list => {
        this.executions = list;
        this.loading = false;
      },
      () => {
        this.executions = [];
        this.loading = false;
      }
    );
  }
}
