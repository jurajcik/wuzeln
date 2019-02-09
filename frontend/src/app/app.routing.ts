import {RouterModule, Routes} from '@angular/router';
import {MatchDetailComponent} from "./components/match/match-detail/match-detail.component";
import {MatchesComponent} from "./components/matches/matches.component";
import {CreateMatchPageComponent} from "./components/creation/create-match-page/create-match-page.component";
import {RegistrationComponent} from "./components/registration/registration.component";
import {ScoresComponent} from "./components/scores/scores.component";
import {OAuthGuard} from "./guards/o-auth.guard";
import {PublicPageComponent} from "./components/public-page/public-page.component";
import {RegisteredUserGuard} from "./guards/registered-user.guard";
import {SignUpComponent} from "./components/sign-up/sign-up.component";
import {AdminComponent} from "./components/admin/admin.component";
import {AdminGuard} from "./guards/admin.guard";

const routes: Routes = [

  {path: 'public', component: PublicPageComponent},

  {path: 'signup', canActivate: [OAuthGuard], component: SignUpComponent},

  {path: '', canActivate: [RegisteredUserGuard], component: MatchesComponent},
  {path: 'matches', canActivate: [RegisteredUserGuard], component: MatchesComponent},
  {path: 'matches/:id', canActivate: [RegisteredUserGuard], component: MatchDetailComponent},
  {path: 'create-match', canActivate: [RegisteredUserGuard], component: CreateMatchPageComponent},
  {path: 'registrations', canActivate: [RegisteredUserGuard], component: MatchesComponent},
  {path: 'registrations/:id', canActivate: [RegisteredUserGuard], component: RegistrationComponent},
  {path: 'scores', canActivate: [RegisteredUserGuard], component: ScoresComponent},

  {path: 'admin', canActivate: [AdminGuard], component: AdminComponent}
];

export const routing = RouterModule.forRoot(routes, {useHash: true});
