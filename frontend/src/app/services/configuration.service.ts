import {Injectable} from '@angular/core';
import {ConfigurationDto} from "./http/httpModel";
import {WuzelnService} from "./http/wuzeln.service";

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {

  private configuration: ConfigurationDto;

  constructor(private wuzelnService: WuzelnService) {
  }

  getConfig(): ConfigurationDto {
    return this.configuration;
  }

  load() {
    return new Promise((resolve, reject) => {
      this.wuzelnService.getConfiguration()
        .subscribe(config => {
          this.configuration = config;
          resolve(true);
        })
    })
  }
}

export function getConfiguration(provider: ConfigurationService) {
  return () => provider.load();
}
