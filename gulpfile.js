var gulp = require('gulp');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var rename = require('gulp-rename');
var insert = require('gulp-insert');

gulp.task('default', function() {
    return gulp.src('guacamole-common-js/src/main/webapp/modules/*.js')
        .pipe(concat('guacamole-common.js'))
        .pipe(insert.append('module.exports = Guacamole;'))
        .pipe(gulp.dest('dist'))
        .pipe(uglify())
        .pipe(rename('guacamole-common.min.js'))
        .pipe(gulp.dest('dist'));
});
