import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, ErrorHandler, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';

import {AppComponent} from './app.component';
import {
  MatBadgeModule,
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDatepickerModule,
  MatDialogModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatGridListModule,
  MatIconModule,
  MatInputModule,
  MatNativeDateModule,
  MatPaginatorModule,
  MatProgressSpinnerModule,
  MatSlideToggleModule,
  MatSortModule,
  MatSpinner,
  MatTableModule,
  MatToolbarModule,
  MatTooltipModule
} from "@angular/material";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {routing} from "./app.routing";
import {NgDragDropModule} from "ng-drag-drop";
import {polyfill} from "mobile-drag-drop";
import {scrollBehaviourDragImageTranslateOverride} from "mobile-drag-drop/scroll-behaviour";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DialogComponent} from './module/dialog/dialog.component';
import {NgHttpLoaderModule} from "ng-http-loader";
import {KatexModule} from "ng-katex";
import {ConfigurationService, getConfiguration} from "./services/configuration.service";
import {CdkDetailRowDirective} from "./components/scores/cdk-detail-row.directive";
import {ScoresComponent} from "./components/scores/scores.component";
import {MatchViewComponent} from "./components/match/match-view/match-view.component";
import {CreateMatchPageComponent} from "./components/creation/create-match-page/create-match-page.component";
import {RegistrationComponent} from "./components/registration/registration.component";
import {MatchFinishedComponent} from "./components/match/match-finished/match-finished.component";
import {MatchDetailComponent} from "./components/match/match-detail/match-detail.component";
import {PositionComponent} from "./components/creation/position/position.component";
import {MatchesComponent} from "./components/matches/matches.component";
import {CreateMatchComponent} from "./components/creation/create-match/create-match.component";
import {getAuthServiceConfigs} from "./services/oauth.service";
import {OAuthInterceptor} from "./interceptors/oauth-interceptor";
import {GoogleApiModule, NG_GAPI_CONFIG} from "ng-gapi";
import {CustomErrorHandler} from "./handlers/custom-error-handler";
import {SignUpComponent} from './components/sign-up/sign-up.component';
import {PublicPageComponent} from './components/public-page/public-page.component';
import {AdminComponent} from './components/admin/admin.component'

@NgModule({
  declarations: [
    AppComponent,
    MatchesComponent,
    CreateMatchComponent,
    PositionComponent,
    MatchDetailComponent,
    MatchFinishedComponent,
    DialogComponent,
    RegistrationComponent,
    CreateMatchPageComponent,
    MatchViewComponent,
    ScoresComponent,
    CdkDetailRowDirective,
    SignUpComponent,
    PublicPageComponent,
    AdminComponent
  ],
  imports: [
    BrowserModule,
    routing,
    HttpClientModule,
    NgHttpLoaderModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatCardModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatGridListModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatCheckboxModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    FormsModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
    BrowserAnimationsModule,
    KatexModule,
    NgDragDropModule.forRoot(),
    GoogleApiModule.forRoot({
      provide: NG_GAPI_CONFIG,
      useFactory: getAuthServiceConfigs,
      deps: [ConfigurationService]
    })
  ],
  providers: [
    ConfigurationService,
    {
      provide: APP_INITIALIZER,
      useFactory: getConfiguration,
      deps: [ConfigurationService],
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: OAuthInterceptor,
      multi: true
    },
    {
      provide: ErrorHandler,
      useClass: CustomErrorHandler
    }
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    DialogComponent,
    MatSpinner
  ]
})

export class AppModule {
}

polyfill({
  // use this to make use of the scroll behaviour
  dragImageTranslateOverride: scrollBehaviourDragImageTranslateOverride
});
