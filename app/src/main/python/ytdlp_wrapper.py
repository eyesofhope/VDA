import yt_dlp
import json

def extract_info(url):
    try:
        ydl_opts = {
            'no_warnings': True,
            'quiet': True,
            'skip_download': True,
        }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            
            formats_list = []
            if 'formats' in info:
                for f in info['formats']:
                    format_id = f.get('format_id') or ''
                    ext = f.get('ext') or ''
                    size = f.get('filesize') or f.get('filesize_approx') or 0
                    acodec = f.get('acodec') or 'none'
                    vcodec = f.get('vcodec') or 'none'
                    
                    # Choose visual label for format / resolution
                    height = f.get('height')
                    width = f.get('width')
                    if height and width:
                        resolution = f"{width}x{height}"
                    elif height:
                        resolution = f"{height}p"
                    else:
                        resolution = "audio"
                        
                    # Filter types
                    if acodec != 'none' and vcodec == 'none':
                        fmt_type = 'audio_only'
                        resolution = "audio (" + (f.get('abr') and f"{int(f['abr'])}kbps" or "HQ") + ")"
                    elif vcodec != 'none' and acodec == 'none':
                        fmt_type = 'video_only'
                    else:
                        fmt_type = 'combined'

                    # Add format if it has download URL
                    if f.get('url'):
                        formats_list.append({
                            'format_id': format_id,
                            'resolution': resolution,
                            'ext': ext,
                            'size': size,
                            'type': fmt_type,
                            'url': f.get('url') or ''
                        })

            # Gather thumbnail
            thumbnail = info.get('thumbnail') or ''
            if not thumbnail and info.get('thumbnails'):
                thumbnail = info['thumbnails'][-1].get('url') or ''

            result = {
                'title': info.get('title') or 'Unknown Title',
                'duration': info.get('duration') or 0,
                'thumbnail': thumbnail,
                'formats': formats_list,
                'uploader': info.get('uploader') or 'Unknown'
            }
            return json.dumps(result)
    except Exception as e:
        return json.dumps({'error': str(e)})

def download_media(url, format_id, outtmpl_path):
    try:
        ydl_opts = {
            'format': format_id,
            'outtmpl': outtmpl_path,
            'no_warnings': True,
            'quiet': True,
        }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        return json.dumps({'success': True})
    except Exception as e:
        return json.dumps({'success': False, 'error': str(e)})
