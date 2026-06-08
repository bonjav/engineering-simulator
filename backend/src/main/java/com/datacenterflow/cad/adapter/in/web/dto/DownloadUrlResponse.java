package com.datacenterflow.cad.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Signed download URL (valid for 1 hour)")
public record DownloadUrlResponse(

    @Schema(description = "Pre-signed URL to download the file directly from storage")
    String url,

    @Schema(description = "URL expiry in seconds")
    int expiresInSeconds
) {

    public static DownloadUrlResponse of(String url) {
        return new DownloadUrlResponse(url, 3600);
    }
}
