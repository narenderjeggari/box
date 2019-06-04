import { Component } from '@angular/core';
import { BoxService } from './box.service';
import { HttpEventType, HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent{
  title = 'Box';
  fileData = null;
  boxData = [];
  constructor(private boxService: BoxService){}
  
  getFilesandFolders(path: string){
    this.boxService.getFilesandFolders(path).subscribe(
      res => {
        this.boxData = res;
      },
      (error) => {
        alert("Request failed");
      }
    );
  }
  downloadFile(item){
    console.log(item);
    this.boxService.downloadFile(item.path).subscribe(
      (response) => {
        this.saveFile(response["cloudElementsLink"], item.name);
      },
      (error) => {
        alert("Request failed");
      }
    );
    
  }

  saveFile(url: string, name: string){
    console.log(url);
    var atag = document.createElement("a");
    atag.href = url;
    atag.download = name;
    atag.click();    
  }
  
  onUploadChange(files: File[]){
    var formData = new FormData();
    formData.append('file', files[0]);
    this.fileData = files[0];  
  }
 
  onSubmit(path: string) {
    const formData = new FormData();
    formData.append('file', this.fileData);
    formData.append('path', path);
    this.boxService.uploadFile(formData).subscribe(
      response =>{
        alert("File Uploaded Sucessfully");
      },
      (error) => {
        alert("Failed to upload file");
      }
    );
  }
}
