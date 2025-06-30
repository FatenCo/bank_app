import { Component, OnInit } from '@angular/core';
import { ActivatedRoute }     from '@angular/router';
import { Location }           from '@angular/common';
import { ProcessService }     from '../../services/process.service';
import { ProcessDefinition }  from '../../models/process-definition.model';

@Component({
  selector: 'app-process-detail',
  templateUrl: './process-detail.component.html',
  styleUrls: ['./process-detail.component.css']
})
export class ProcessDetailComponent implements OnInit {
  pd?: ProcessDefinition;
  definitionId!: string;

  constructor(
    private route: ActivatedRoute,
    private svc: ProcessService,
    private location: Location
  ) {}

  ngOnInit() {
    this.definitionId = this.route.snapshot.paramMap.get('id')!;
    this.svc.get(this.definitionId).subscribe(pd => this.pd = pd);
  }

  back(): void {
    this.location.back();
  }
}
