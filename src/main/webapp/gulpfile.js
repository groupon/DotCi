var gulp = require('gulp')
var browserify = require('browserify');
var source = require("vinyl-source-stream");
var watchify = require('watchify');
var livereload = require('gulp-livereload');
var gulpif = require('gulp-if');
var watch;

gulp.task('browserify-nowatch', function(){
    watch = false;
    browserifyShare();
});

gulp.task('browserify-watch', function(){
    watch = true;
    browserifyShare();
});

function browserifyShare(){
    var b = browserify({
        cache: {},
        packageCache: {},
        fullPaths: true
    });

    if(watch) {
        // if watch is enable, wrap this bundle inside watchify
        b = watchify(b);
        b.on('update', function(){
            bundleShare(b);
        });
    }

    b.add('./js/main.js');
    bundleShare(b);
}

function bundleShare(b) {
    b.bundle()
        .pipe(source('dotci.js'))
        .pipe(gulp.dest('./js'))
        .pipe(gulpif(watch, livereload()));
}

// define the browserify-watch as dependencies for this task
gulp.task('watch', ['browserify-watch'], function(){
    // watch other files, for example .less file
    gulp.watch('./less/*.less',
        ['compile-less']);

    // Start live reload server
    livereload.listen(35729);
});