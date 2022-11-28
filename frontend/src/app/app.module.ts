import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActorSearchComponent } from './actor-search/actor-search.component';
import { MovieSearchComponent } from './movie-search/movie-search.component';
import { TopMenuComponent } from './top-menu/top-menu.component';
import {MatCardModule} from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule, MatList, MatListModule, MatProgressBarModule } from '@angular/material';
@NgModule({
  declarations: [
    AppComponent,
    ActorSearchComponent,
    MovieSearchComponent,
    TopMenuComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    MatCardModule,
    MatToolbarModule,
    MatDividerModule,
    MatProgressBarModule,
    MatListModule,
  MatButtonModule 
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
